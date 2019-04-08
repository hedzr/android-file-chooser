package com.obsez.android.lib.filechooser;

import static com.obsez.android.lib.filechooser.ChooserDialog.sPrimaryStorage;
import static com.obsez.android.lib.filechooser.ChooserDialog.sSdcardStorage;

import android.app.AlertDialog;
import android.app.Dialog;

import java.lang.ref.WeakReference;

class defBackPressed implements ChooserDialog.OnBackPressedListener {
    private WeakReference<ChooserDialog> _c;

    defBackPressed(ChooserDialog e) {
        this._c = new WeakReference<>(e);
    }

    @Override
    public void onBackPressed(AlertDialog dialog) {
        if (_c.get()._entries.size() > 0
            && (_c.get()._entries.get(0).getName().equals(".."))) {
            _c.get().onItemClick(null, _c.get()._list, 0, 0);
        } else {
            if (_onLastBackPressed != null) {
                _onLastBackPressed.onBackPressed(dialog);
            } else {
                _defaultLastBack.onBackPressed(dialog);
            }
        }
    }

    ChooserDialog.OnBackPressedListener _onLastBackPressed;

    ChooserDialog.OnBackPressedListener _defaultLastBack = Dialog::dismiss;
}
