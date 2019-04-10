package com.obsez.android.lib.filechooser;

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
            if (_onBackPressed != null) {
                _onBackPressed.onBackPressed(dialog);
            } else {
                _defaultBack.onBackPressed(dialog);
            }
        } else {
            if (_onLastBackPressed != null) {
                _onLastBackPressed.onBackPressed(dialog);
            } else {
                _defaultLastBack.onBackPressed(dialog);
            }
        }
    }

    ChooserDialog.OnBackPressedListener _onBackPressed;
    ChooserDialog.OnBackPressedListener _onLastBackPressed;

    private static final ChooserDialog.OnBackPressedListener _defaultLastBack = Dialog::cancel;
    private static final ChooserDialog.OnBackPressedListener _defaultBack = Dialog::cancel;
}
