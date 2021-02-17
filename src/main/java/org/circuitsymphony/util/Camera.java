package org.circuitsymphony.util;

/**
 * Orthographic camera implementation providing utilities method to convert point between screen and world coordinates systems.
 */
public class Camera {
    public double zoom = 1;
    public Vector3 position = new Vector3();
    private Vector3 tmpVec = new Vector3();
    private Matrix4 projection = new Matrix4();
    private Matrix4 view = new Matrix4();
    private Matrix4 combined = new Matrix4();
    private Matrix4 invProjectionView = new Matrix4();

    private Vector3 direction = new Vector3(0, 0, -1);
    private Vector3 up = new Vector3(0, 1, 0);

    private double viewportWidth = 0;
    private double viewportHeight = 0;

    public void setToOrtho(double viewportWidth, double viewportHeight) {
        position.set(zoom * viewportWidth / 2.0f, zoom * viewportHeight / 2.0f, 0);
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        update();
    }

    public void update() {
        projection.setToOrtho(zoom * -viewportWidth / 2, zoom * (viewportWidth / 2), zoom * -(viewportHeight / 2), zoom * viewportHeight / 2, 0, 100);
        view.setToLookAt(position, tmpVec.set(position).add(direction), up);
        combined.set(projection);
        combined.multiply(view);

        invProjectionView.set(combined);
        invProjectionView = invProjectionView.invert();
    }

    public Vector3 unproject(Vector3 screenCoords) {
        double x = screenCoords.x, y = screenCoords.y;
        screenCoords.x = (2 * x) / viewportWidth - 1;
        screenCoords.y = (2 * y) / viewportHeight - 1;
        screenCoords.z = 2 * screenCoords.z - 1;
        screenCoords.project(invProjectionView);
        return screenCoords;
    }


    public Vector3 project(Vector3 worldCoords) {
        worldCoords.project(combined);
        worldCoords.x = viewportWidth * (worldCoords.x + 1) / 2;
        worldCoords.y = viewportHeight * (worldCoords.y + 1) / 2;
        worldCoords.z = (worldCoords.z + 1) / 2;
        return worldCoords;
    }

    public void updateViewport(double viewportWidth, double viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        update();
    }

    public static class Vector3 {
        public double x;
        public double y;
        public double z;

        public Vector3() {
        }

        public Vector3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector3 add(Vector3 other) {
            return set(x + other.x, y + other.y, z + other.z);
        }

        public Vector3 sub(Vector3 other) {
            return set(x - other.x, y - other.y, z - other.z);
        }

        public Vector3 set(Vector3 other) {
            return set(other.x, other.y, other.z);
        }

        public Vector3 set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public Vector3 normalize() {
            final double len2 = this.lengthSquared();
            if (len2 == 0f || len2 == 1f) return this;
            return this.scale(1f / Math.sqrt(len2));
        }

        /**
         * @return cross product of two vectors
         */
        public Vector3 crossProduct(final Vector3 vector) {
            return this.set(y * vector.z - z * vector.y, z * vector.x - x * vector.z, x * vector.y - y * vector.x);
        }

        /**
         * Scales this vector by passed scalar value
         */
        public Vector3 scale(double scalar) {
            return this.set(this.x * scalar, this.y * scalar, this.z * scalar);
        }

        /**
         * @return squared length
         */
        public double lengthSquared() {
            return x * x + y * y + z * z;
        }

        /**
         * Projects this vector against matrix
         */
        public Vector3 project(final Matrix4 matrix) {
            double val[][] = matrix.getValues();
            double lW = 1f / (x * val[3][0] + y * val[3][1] + z * val[3][2] + val[3][3]);

            return this.set(
                    (x * val[0][0] + y * val[0][1] + z * val[0][2] + val[0][3]) * lW,
                    (x * val[1][0] + y * val[1][1] + z * val[1][2] + val[1][3]) * lW,
                    (x * val[2][0] + y * val[2][1] + z * val[2][2] + val[2][3]) * lW);
        }
    }

    private static class Matrix4 {
        static final Vector3 l_vez = new Vector3();
        static final Vector3 l_vex = new Vector3();
        static final Vector3 l_vey = new Vector3();
        private static final Vector3 tmpVec = new Vector3();
        private static final Matrix4 tmpMat = new Matrix4();
        private double[][] val;

        public Matrix4() {
            val = new double[4][4];
        }

        public Matrix4(double[][] data) {
            this();
            set(data);
        }

        /**
         * Initialize this matrix as identity matrix
         */
        public void setToIdentity() {
            val[0][0] = 1;
            val[0][1] = 0;
            val[0][2] = 0;
            val[0][3] = 0;
            val[1][0] = 0;
            val[1][1] = 1;
            val[1][2] = 0;
            val[1][3] = 0;
            val[2][0] = 0;
            val[2][1] = 0;
            val[2][2] = 1;
            val[2][3] = 0;
            val[3][0] = 0;
            val[3][1] = 0;
            val[3][2] = 0;
            val[3][3] = 1;
        }

        /**
         * Sets this matrix to a standard translation matrix.
         */
        public Matrix4 setToTranslation(double x, double y, double z) {
            setToIdentity();
            val[0][3] = x;
            val[1][3] = y;
            val[2][3] = z;
            return this;
        }

        public Matrix4 setToLookAt(Vector3 position, Vector3 target, Vector3 up) {
            tmpVec.set(target).sub(position);
            setToLookAt(tmpVec, up);
            multiply(tmpMat.setToTranslation(-position.x, -position.y, -position.z));
            return this;
        }

        public Matrix4 setToLookAt(Vector3 direction, Vector3 up) {
            l_vez.set(direction).normalize();
            l_vex.set(direction).normalize();
            l_vex.crossProduct(up).normalize();
            l_vey.set(l_vex).crossProduct(l_vez).normalize();
            setToIdentity();
            val[0][0] = l_vex.x;
            val[0][1] = l_vex.y;
            val[0][2] = l_vex.z;
            val[1][0] = l_vey.x;
            val[1][1] = l_vey.y;
            val[1][2] = l_vey.z;
            val[2][0] = -l_vez.x;
            val[2][1] = -l_vez.y;
            val[2][2] = -l_vez.z;
            return this;
        }

        /**
         * Sets the matrix to an orthographic projection like glOrtho (https://www.opengl.org/sdk/docs/man2/xhtml/glOrtho.xml).
         */
        public Matrix4 setToOrtho(double left, double right, double bottom, double top, double near, double far) {
            setToIdentity();
            double x_orth = 2 / (right - left);
            double y_orth = 2 / (top - bottom);
            double z_orth = -2 / (far - near);

            double tx = -(right + left) / (right - left);
            double ty = -(top + bottom) / (top - bottom);
            double tz = -(far + near) / (far - near);

            val[0][0] = x_orth;
            val[1][0] = 0;
            val[2][0] = 0;
            val[3][0] = 0;
            val[0][1] = 0;
            val[1][1] = y_orth;
            val[2][1] = 0;
            val[3][1] = 0;
            val[0][2] = 0;
            val[1][2] = 0;
            val[2][2] = z_orth;
            val[3][2] = 0;
            val[0][3] = tx;
            val[1][3] = ty;
            val[2][3] = tz;
            val[3][3] = 1;
            return this;
        }

        /**
         * Sets values of this matrix to passed values
         */
        public Matrix4 set(Matrix4 other) {
            return set(other.val);
        }

        /**
         * Sets values of this matrix to passed values
         */
        public Matrix4 set(double[][] values) {
            for (int i = 0; i < val.length; i++)
                System.arraycopy(values[i], 0, val[i], 0, val[i].length);
            return this;
        }

        /**
         * Multiples this matrix with some other matrix
         */
        public Matrix4 multiply(Matrix4 other) {
            int rows = val.length;
            int columns = val[0].length;
            int otherRows = other.val.length;
            int otherColumns = other.val[0].length;
            if (columns != otherRows) {
                throw new IllegalArgumentException("This matrix rows number must match other matrix columns number.");
            }

            double[][] newVal = new double[rows][otherColumns];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < otherColumns; j++) {
                    for (int k = 0; k < columns; k++) {
                        newVal[i][j] += val[i][k] * other.val[k][j];
                    }
                }
            }
            set(newVal);
            return this;
        }

        /**
         * @return reference to internal matrix value array
         */
        public double[][] getValues() {
            return val;
        }

        /**
         * Inverts this matrix.
         */
        public Matrix4 invert() {
            double lDet = val[3][0] * val[2][1] * val[1][2] * val[0][3] - val[2][0] * val[3][1] * val[1][2] * val[0][3] - val[3][0] * val[1][1]
                    * val[2][2] * val[0][3] + val[1][0] * val[3][1] * val[2][2] * val[0][3] + val[2][0] * val[1][1] * val[3][2] * val[0][3] - val[1][0]
                    * val[2][1] * val[3][2] * val[0][3] - val[3][0] * val[2][1] * val[0][2] * val[1][3] + val[2][0] * val[3][1] * val[0][2] * val[1][3]
                    + val[3][0] * val[0][1] * val[2][2] * val[1][3] - val[0][0] * val[3][1] * val[2][2] * val[1][3] - val[2][0] * val[0][1] * val[3][2]
                    * val[1][3] + val[0][0] * val[2][1] * val[3][2] * val[1][3] + val[3][0] * val[1][1] * val[0][2] * val[2][3] - val[1][0] * val[3][1]
                    * val[0][2] * val[2][3] - val[3][0] * val[0][1] * val[1][2] * val[2][3] + val[0][0] * val[3][1] * val[1][2] * val[2][3] + val[1][0]
                    * val[0][1] * val[3][2] * val[2][3] - val[0][0] * val[1][1] * val[3][2] * val[2][3] - val[2][0] * val[1][1] * val[0][2] * val[3][3]
                    + val[1][0] * val[2][1] * val[0][2] * val[3][3] + val[2][0] * val[0][1] * val[1][2] * val[3][3] - val[0][0] * val[2][1] * val[1][2]
                    * val[3][3] - val[1][0] * val[0][1] * val[2][2] * val[3][3] + val[0][0] * val[1][1] * val[2][2] * val[3][3];
            if (lDet == 0f) throw new IllegalStateException("Attempted to invert non-invertible matrix");
            double invDet = 1.0 / lDet;
            double[][] tmp = tmpMat.val;
            tmp[0][0] = val[1][2] * val[2][3] * val[3][1] - val[1][3] * val[2][2] * val[3][1] + val[1][3] * val[2][1]
                    * val[3][2] - val[1][1] * val[2][3] * val[3][2] - val[1][2] * val[2][1] * val[3][3] + val[1][1] * val[2][2] * val[3][3];
            tmp[0][1] = val[0][3] * val[2][2] * val[3][1] - val[0][2] * val[2][3] * val[3][1] - val[0][3] * val[2][1]
                    * val[3][2] + val[0][1] * val[2][3] * val[3][2] + val[0][2] * val[2][1] * val[3][3] - val[0][1] * val[2][2] * val[3][3];
            tmp[0][2] = val[0][2] * val[1][3] * val[3][1] - val[0][3] * val[1][2] * val[3][1] + val[0][3] * val[1][1]
                    * val[3][2] - val[0][1] * val[1][3] * val[3][2] - val[0][2] * val[1][1] * val[3][3] + val[0][1] * val[1][2] * val[3][3];
            tmp[0][3] = val[0][3] * val[1][2] * val[2][1] - val[0][2] * val[1][3] * val[2][1] - val[0][3] * val[1][1]
                    * val[2][2] + val[0][1] * val[1][3] * val[2][2] + val[0][2] * val[1][1] * val[2][3] - val[0][1] * val[1][2] * val[2][3];
            tmp[1][0] = val[1][3] * val[2][2] * val[3][0] - val[1][2] * val[2][3] * val[3][0] - val[1][3] * val[2][0]
                    * val[3][2] + val[1][0] * val[2][3] * val[3][2] + val[1][2] * val[2][0] * val[3][3] - val[1][0] * val[2][2] * val[3][3];
            tmp[1][1] = val[0][2] * val[2][3] * val[3][0] - val[0][3] * val[2][2] * val[3][0] + val[0][3] * val[2][0]
                    * val[3][2] - val[0][0] * val[2][3] * val[3][2] - val[0][2] * val[2][0] * val[3][3] + val[0][0] * val[2][2] * val[3][3];
            tmp[1][2] = val[0][3] * val[1][2] * val[3][0] - val[0][2] * val[1][3] * val[3][0] - val[0][3] * val[1][0]
                    * val[3][2] + val[0][0] * val[1][3] * val[3][2] + val[0][2] * val[1][0] * val[3][3] - val[0][0] * val[1][2] * val[3][3];
            tmp[1][3] = val[0][2] * val[1][3] * val[2][0] - val[0][3] * val[1][2] * val[2][0] + val[0][3] * val[1][0]
                    * val[2][2] - val[0][0] * val[1][3] * val[2][2] - val[0][2] * val[1][0] * val[2][3] + val[0][0] * val[1][2] * val[2][3];
            tmp[2][0] = val[1][1] * val[2][3] * val[3][0] - val[1][3] * val[2][1] * val[3][0] + val[1][3] * val[2][0]
                    * val[3][1] - val[1][0] * val[2][3] * val[3][1] - val[1][1] * val[2][0] * val[3][3] + val[1][0] * val[2][1] * val[3][3];
            tmp[2][1] = val[0][3] * val[2][1] * val[3][0] - val[0][1] * val[2][3] * val[3][0] - val[0][3] * val[2][0]
                    * val[3][1] + val[0][0] * val[2][3] * val[3][1] + val[0][1] * val[2][0] * val[3][3] - val[0][0] * val[2][1] * val[3][3];
            tmp[2][2] = val[0][1] * val[1][3] * val[3][0] - val[0][3] * val[1][1] * val[3][0] + val[0][3] * val[1][0]
                    * val[3][1] - val[0][0] * val[1][3] * val[3][1] - val[0][1] * val[1][0] * val[3][3] + val[0][0] * val[1][1] * val[3][3];
            tmp[2][3] = val[0][3] * val[1][1] * val[2][0] - val[0][1] * val[1][3] * val[2][0] - val[0][3] * val[1][0]
                    * val[2][1] + val[0][0] * val[1][3] * val[2][1] + val[0][1] * val[1][0] * val[2][3] - val[0][0] * val[1][1] * val[2][3];
            tmp[3][0] = val[1][2] * val[2][1] * val[3][0] - val[1][1] * val[2][2] * val[3][0] - val[1][2] * val[2][0]
                    * val[3][1] + val[1][0] * val[2][2] * val[3][1] + val[1][1] * val[2][0] * val[3][2] - val[1][0] * val[2][1] * val[3][2];
            tmp[3][1] = val[0][1] * val[2][2] * val[3][0] - val[0][2] * val[2][1] * val[3][0] + val[0][2] * val[2][0]
                    * val[3][1] - val[0][0] * val[2][2] * val[3][1] - val[0][1] * val[2][0] * val[3][2] + val[0][0] * val[2][1] * val[3][2];
            tmp[3][2] = val[0][2] * val[1][1] * val[3][0] - val[0][1] * val[1][2] * val[3][0] - val[0][2] * val[1][0]
                    * val[3][1] + val[0][0] * val[1][2] * val[3][1] + val[0][1] * val[1][0] * val[3][2] - val[0][0] * val[1][1] * val[3][2];
            tmp[3][3] = val[0][1] * val[1][2] * val[2][0] - val[0][2] * val[1][1] * val[2][0] + val[0][2] * val[1][0]
                    * val[2][1] - val[0][0] * val[1][2] * val[2][1] - val[0][1] * val[1][0] * val[2][2] + val[0][0] * val[1][1] * val[2][2];
            val[0][0] = tmp[0][0] * invDet;
            val[0][1] = tmp[0][1] * invDet;
            val[0][2] = tmp[0][2] * invDet;
            val[0][3] = tmp[0][3] * invDet;
            val[1][0] = tmp[1][0] * invDet;
            val[1][1] = tmp[1][1] * invDet;
            val[1][2] = tmp[1][2] * invDet;
            val[1][3] = tmp[1][3] * invDet;
            val[2][0] = tmp[2][0] * invDet;
            val[2][1] = tmp[2][1] * invDet;
            val[2][2] = tmp[2][2] * invDet;
            val[2][3] = tmp[2][3] * invDet;
            val[3][0] = tmp[3][0] * invDet;
            val[3][1] = tmp[3][1] * invDet;
            val[3][2] = tmp[3][2] * invDet;
            val[3][3] = tmp[3][3] * invDet;
            return this;
        }
    }
}
