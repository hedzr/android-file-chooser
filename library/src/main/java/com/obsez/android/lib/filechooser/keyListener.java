package com.obsez.android.lib.filechooser;

import android.content.DialogInterface;
import android.view.KeyEvent;

import java.lang.ref.WeakReference;

class keyListener implements DialogInterface.OnKeyListener {
    private WeakReference<ChooserDialog> _c;

    keyListener(ChooserDialog c) {
        this._c = new WeakReference<>(c);
    }

    /**
     * Called when a key is dispatched to a dialog. This allows listeners to
     * get a chance to respond before the dialog.
     *
     * @param dialog  the dialog the key has been dispatched to
     * @param keyCode the code for the physical key that was pressed
     * @param event   the KeyEvent object containing full information about
     *                the event
     * @return {@code true} if the listener has consumed the event,
     * {@code false} otherwise
     */
    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        switch (keyCode) {
            //case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    return _c.get().cancelFolderViewOrBack(dialog);
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    return _c.get().doMoveUp();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    return _c.get().doMoveDown();
                }
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                // go back
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    return _c.get().doGoBack();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                // enter
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (!_c.get().buttonsHasFocus()) {
                        return _c.get().doEnter();
                    }
                }
                break;
        }
        return false;
    }
}
