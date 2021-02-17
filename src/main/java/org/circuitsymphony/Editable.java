package org.circuitsymphony;

import org.circuitsymphony.ui.EditInfo;


public interface Editable {
    EditInfo getEditInfo(int n);

    void setEditValue(int n, EditInfo ei);
}
