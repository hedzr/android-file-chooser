package com.obsez.android.lib.filechooser;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.internals.ExtFileFilter;
import com.obsez.android.lib.filechooser.internals.FileUtil;
import com.obsez.android.lib.filechooser.internals.RegexFileFilter;
import com.obsez.android.lib.filechooser.internals.UiUtil;
import com.obsez.android.lib.filechooser.tool.DirAdapter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER;
import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.Gravity.END;
import static android.view.Gravity.START;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
import static com.obsez.android.lib.filechooser.internals.FileUtil.NewFolderFilter;

/**
 * Created by coco on 6/7/15.
 */
public class ChooserDialog implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener,
    AdapterView.OnItemLongClickListener, AdapterView.OnItemSelectedListener, DialogInterface.OnKeyListener {
    @FunctionalInterface
    public interface Result {
        void onChoosePath(String dir, File dirFile);
    }

    public ChooserDialog() {

    }

    public ChooserDialog(Context cxt) {
        this._context = cxt;
    }

    public ChooserDialog(Activity activity) {
        this._context = activity;
    }

    public ChooserDialog(Fragment fragment) {
        this._context = fragment.getActivity();
    }

    /**
     * @param cxt android context
     * @return `this` reference
     * @deprecated will be removed at v1.2
     */
    public ChooserDialog with(Context cxt) {
        this._context = cxt;
        return this;
    }

    public ChooserDialog withFilter(FileFilter ff) {
        withFilter(false, false, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    public ChooserDialog withFilter(boolean dirOnly, boolean allowHidden, FileFilter ff) {
        withFilter(dirOnly, allowHidden, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    public ChooserDialog withFilter(boolean allowHidden, String... suffixes) {
        return withFilter(false, allowHidden, suffixes);
    }

    public ChooserDialog withFilter(boolean dirOnly, final boolean allowHidden, String... suffixes) {
        this._dirOnly = dirOnly;
        if (suffixes == null || suffixes.length == 0) {
            this._fileFilter = dirOnly ?
                new FileFilter() {
                    @Override
                    public boolean accept(final File file) {
                        return file.isDirectory() && (!file.isHidden() || allowHidden);
                    }
                } : new FileFilter() {
                @Override
                public boolean accept(final File file) {
                    return !file.isHidden() || allowHidden;
                }
            };
        } else {
            this._fileFilter = new ExtFileFilter(_dirOnly, allowHidden, suffixes);
        }
        return this;
    }

    public ChooserDialog withFilterRegex(boolean dirOnly, boolean allowHidden, String pattern, int flags) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, flags);
        return this;
    }

    public ChooserDialog withFilterRegex(boolean dirOnly, boolean allowHidden, String pattern) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, Pattern.CASE_INSENSITIVE);
        return this;
    }

    public ChooserDialog withStartFile(String startFile) {
        if (startFile != null) {
            _currentDir = new File(startFile);
        } else {
            _currentDir = Environment.getExternalStorageDirectory();
        }

        if (!_currentDir.isDirectory()) {
            _currentDir = _currentDir.getParentFile();
        }

        if (_currentDir == null) {
            _currentDir = Environment.getExternalStorageDirectory();
        }

        return this;
    }

    public ChooserDialog dismissOnButtonClick(final boolean dismissOnButtonClick) {
        this._dismissOnButtonClick = dismissOnButtonClick;
        if (dismissOnButtonClick) {
            this._defaultLastBack = new OnBackPressedListener() {
                @Override
                public void onBackPressed(AlertDialog dialog) {
                    dialog.dismiss();
                }
            };
        } else {
            this._defaultLastBack = new OnBackPressedListener() {
                @Override
                public void onBackPressed(AlertDialog dialog) {
                    //
                }
            };
        }
        return this;
    }

    public ChooserDialog withChosenListener(Result r) {
        this._result = r;
        return this;
    }

    public ChooserDialog withOnBackPressedListener(OnBackPressedListener listener) {
        this._onBackPressed = listener;
        return this;
    }

    public ChooserDialog withOnLastBackPressedListener(OnBackPressedListener listener) {
        this._onLastBackPressed = listener;
        return this;
    }

    public ChooserDialog withResources(@StringRes int titleRes, @StringRes int okRes, @StringRes int cancelRes) {
        this._titleRes = titleRes;
        this._okRes = okRes;
        this._negativeRes = cancelRes;
        return this;
    }

    public ChooserDialog enableOptions(boolean enableOptions) {
        this._enableOptions = enableOptions;
        return this;
    }

    public ChooserDialog withOptionResources(@StringRes int createDirRes, @StringRes int deleteRes,
                                             @StringRes int newFolderCancelRes, @StringRes int newFolderOkRes) {
        this._createDirRes = createDirRes;
        this._deleteRes = deleteRes;
        this._newFolderCancelRes = newFolderCancelRes;
        this._newFolderOkRes = newFolderOkRes;
        return this;
    }

    public ChooserDialog withOptionIcons(@DrawableRes int optionsIconRes, @DrawableRes int createDirIconRes,
                                         @DrawableRes int deleteRes) {
        this._optionsIconRes = optionsIconRes;
        this._createDirIconRes = createDirIconRes;
        this._deleteIconRes = deleteRes;
        return this;
    }

    public ChooserDialog withNewFolderFilter(NewFolderFilter filter) {
        this._newFolderFilter = filter;
        return this;
    }

    public ChooserDialog withIcon(@DrawableRes int iconId) {
        this._iconRes = iconId;
        return this;
    }

    public ChooserDialog withLayoutView(@LayoutRes int layoutResId) {
        this._layoutRes = layoutResId;
        return this;
    }

    public ChooserDialog withRowLayoutView(@LayoutRes int layoutResId) {
        this._rowLayoutRes = layoutResId;
        return this;
    }

    public ChooserDialog withDateFormat() {
        return this.withDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    public ChooserDialog withDateFormat(String format) {
        this._dateFormat = format;
        return this;
    }

    public ChooserDialog withNegativeButton(@StringRes int cancelTitle,
                                            final DialogInterface.OnClickListener listener) {
        this._negativeRes = cancelTitle;
        this._negativeListener = listener;
        return this;
    }

    public ChooserDialog withNegativeButtonListener(final DialogInterface.OnClickListener listener) {
        this._negativeListener = listener;
        return this;
    }

    /**
     * it's NOT recommended to use the `withOnCancelListener`, replace with `withNegativeButtonListener` pls.
     *
     * @deprecated will be removed at v1.2
     */
    public ChooserDialog withOnCancelListener(final DialogInterface.OnCancelListener listener) {
        this._cancelListener2 = listener;
        return this;
    }

    public ChooserDialog withOnDismissListener(final DialogInterface.OnDismissListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            _onDismissListener = listener;
        }
        return this;
    }

    public ChooserDialog withFileIcons(final boolean tryResolveFileTypeAndIcon, final Drawable fileIcon,
                                       final Drawable folderIcon) {
        _adapterSetter = new AdapterSetter() {
            @Override
            public void apply(DirAdapter adapter) {
                if (fileIcon != null) adapter.setDefaultFileIcon(fileIcon);
                if (folderIcon != null) adapter.setDefaultFolderIcon(folderIcon);
                adapter.setResolveFileType(tryResolveFileTypeAndIcon);
            }
        };
        return this;
    }

    public ChooserDialog withFileIconsRes(final boolean tryResolveFileTypeAndIcon, final int fileIcon,
                                          final int folderIcon) {
        _adapterSetter = new AdapterSetter() {
            @Override
            public void apply(DirAdapter adapter) {
                if (fileIcon != -1) {
                    adapter.setDefaultFileIcon(ContextCompat.getDrawable(_context, fileIcon));
                }
                if (folderIcon != -1) {
                    adapter.setDefaultFolderIcon(
                        ContextCompat.getDrawable(_context, folderIcon));
                }
                adapter.setResolveFileType(tryResolveFileTypeAndIcon);
            }
        };
        return this;
    }

    /**
     * @param setter you can customize the folder navi-adapter with `setter`
     * @return this
     */
    public ChooserDialog withAdapterSetter(AdapterSetter setter) {
        _adapterSetter = setter;
        return this;
    }

    /**
     * @param cb give a hook at navigating up to a directory
     * @return this
     */
    public ChooserDialog withNavigateUpTo(CanNavigateUp cb) {
        _folderNavUpCB = cb;
        return this;
    }

    /**
     * @param cb give a hook at navigating to a child directory
     * @return this
     */
    public ChooserDialog withNavigateTo(CanNavigateTo cb) {
        _folderNavToCB = cb;
        return this;
    }

    public ChooserDialog disableTitle(boolean disableTitle) {
        _disableTitle = disableTitle;
        return this;
    }

    public ChooserDialog enableMultiple(boolean enableMultiple) {
        this._enableMultiple = enableMultiple;
        return this;
    }

    public ChooserDialog enableDpad(boolean enableDpad){
        this._enableDpad = enableDpad;
        return this;
    }

    public ChooserDialog build() {
        if (_titleRes == 0 || _okRes == 0 || _negativeRes == 0) {
            throw new RuntimeException("withResources() should be called at first.");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(_context);

        _adapter = new DirAdapter(_context, new ArrayList<File>(),
            _rowLayoutRes != -1 ? _rowLayoutRes : R.layout.li_row_textview, this._dateFormat);
        if (_adapterSetter != null) _adapterSetter.apply(_adapter);
        refreshDirs();
        builder.setAdapter(_adapter, this);

        if (!_disableTitle) {
            builder.setTitle(_titleRes);
        }

        if (_iconRes != -1) {
            builder.setIcon(_iconRes);
        }

        if (_layoutRes != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setView(_layoutRes);
            }
        }

        if (_dirOnly || _enableMultiple) {
            builder.setPositiveButton(_okRes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (_result != null) {
                        _result.onChoosePath(_currentDir.getAbsolutePath(), _currentDir);
                    }
                }
            });
        }

        builder.setNegativeButton(_negativeRes, _negativeListener);

        if (_cancelListener2 != null) {
            builder.setOnCancelListener(_cancelListener2);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && _onDismissListener != null) {
            builder.setOnDismissListener(_onDismissListener);
        }

        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    if (_newFolderView != null && _newFolderView.getVisibility() == View.VISIBLE) {
                        _newFolderView.setVisibility(View.INVISIBLE);
                        return true;
                    }

                    _onBackPressed.onBackPressed((AlertDialog) dialog);
                }
                return true;
            }
        });

        _alertDialog = builder.create();

        setupOnShowListener();
        _list = _alertDialog.getListView();
        _list.setOnItemClickListener(this);
        if (_enableMultiple) {
            _list.setOnItemLongClickListener(this);
        }

        if(_enableDpad){
            _list.setSelector(R.drawable.listview_item_selector);
            _list.setDrawSelectorOnTop(true);
            _list.setItemsCanFocus(true);
            _list.setOnItemSelectedListener(this);
            _alertDialog.setOnKeyListener(this);
        }
        return this;
    }

    private void setupOnShowListener() {
        _alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                if (!_dismissOnButtonClick) {
                    Button negative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                    negative.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            if (_negativeListener != null) {
                                _negativeListener.onClick(_alertDialog, AlertDialog.BUTTON_NEGATIVE);
                            }
                        }
                    });

                    positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            if (_result != null) {
                                if (_dirOnly || _enableMultiple) {
                                    _result.onChoosePath(_currentDir.getAbsolutePath(), _currentDir);
                                }
                            }
                        }
                    });
                }

                if (_createDirRes == 0 || _newFolderCancelRes == 0 || _newFolderOkRes == 0) {
                    throw new RuntimeException("withOptionResources() should be called at first.");
                }

                if (_enableOptions) {
                    final int color = UiUtil.getThemeAccentColor(_context);
                    final PorterDuffColorFilter filter = new PorterDuffColorFilter(color,
                        PorterDuff.Mode.SRC_IN);

                    final Button options = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                    options.setText("");
                    options.setTextColor(color);
                    options.setVisibility(View.VISIBLE);
                    final Drawable drawable = ContextCompat.getDrawable(_context,
                        _optionsIconRes != -1 ? _optionsIconRes : R.drawable.ic_menu_24dp);
                    if (drawable != null) {
                        drawable.setColorFilter(filter);
                        options.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                    } else {
                        options.setCompoundDrawablesWithIntrinsicBounds(
                            _optionsIconRes != -1 ? _optionsIconRes : R.drawable.ic_menu_24dp, 0, 0, 0);
                    }

                    final Runnable showOptions = new Runnable() {
                        @Override
                        public void run() {
                            final ViewGroup.MarginLayoutParams params =
                                (ViewGroup.MarginLayoutParams) _list.getLayoutParams();
                            if (_options.getHeight() == 0) {
                                ViewTreeObserver viewTreeObserver = _options.getViewTreeObserver();
                                viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                                    @Override
                                    public boolean onPreDraw() {
                                        if (_options.getHeight() <= 0) {
                                            return false;
                                        }
                                        _options.getViewTreeObserver().removeOnPreDrawListener(this);
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                params.bottomMargin = _options.getHeight();
                                                _list.setLayoutParams(params);
                                                _options.setVisibility(View.VISIBLE);
                                                _options.requestFocus();
                                            }
                                        }, 100);
                                        return true;
                                    }
                                });
                            } else {
                                params.bottomMargin = _options.getHeight();
                                _list.setLayoutParams(params);
                                _options.setVisibility(View.VISIBLE);
                                _options.requestFocus();
                            }
                        }
                    };
                    final Runnable hideOptions = new Runnable() {
                        @Override
                        public void run() {
                            _options.setVisibility(View.INVISIBLE);
                            _options.clearFocus();
                            ViewGroup.MarginLayoutParams params =
                                (ViewGroup.MarginLayoutParams) _list.getLayoutParams();
                            params.bottomMargin = 0;
                            _list.setLayoutParams(params);
                        }
                    };

                    options.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            if (_options == null) {
                                // region Draw options view. (this only happens the first time one clicks on
                                // options)
                                // Root view (FrameLayout) of the ListView in the AlertDialog.
                                final int rootId = _context.getResources().getIdentifier("contentPanel", "id",
                                    "android");
                                final FrameLayout root = ((AlertDialog) dialog).findViewById(rootId);
                                // In case the was changed or not found.
                                if (root == null) return;

                                // Create options view.
                                final FrameLayout options = new FrameLayout(_context);
                                //options.setBackgroundColor(0x60000000);
                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(MATCH_PARENT,
                                    WRAP_CONTENT, BOTTOM);
                                root.addView(options, params);

                                options.setOnClickListener(null);
                                options.setVisibility(View.INVISIBLE);
                                _options = options;

                                // Create a button for the option to create a new directory/folder.
                                final Button createDir = new Button(_context, null,
                                    android.R.attr.buttonBarButtonStyle);
                                createDir.setText(_createDirRes);
                                createDir.setTextColor(color);
                                final Drawable plus = ContextCompat.getDrawable(_context,
                                    _createDirIconRes != -1 ? _createDirIconRes : R.drawable.ic_add_24dp);
                                if (plus != null) {
                                    plus.setColorFilter(filter);
                                    createDir.setCompoundDrawablesWithIntrinsicBounds(plus, null, null, null);
                                } else {
                                    createDir.setCompoundDrawablesWithIntrinsicBounds(
                                        _createDirIconRes != -1 ? _createDirIconRes : R.drawable.ic_add_24dp, 0,
                                        0, 0);
                                }
                                if(_enableDpad){
                                    createDir.setBackgroundResource(R.drawable.listview_item_selector);
                                }
                                params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT,
                                    START | CENTER_VERTICAL);
                                params.leftMargin = 10;
                                options.addView(createDir, params);

                                // Create a button for the option to delete a file.
                                final Button delete = new Button(_context, null,
                                    android.R.attr.buttonBarButtonStyle);
                                delete.setText(_deleteRes);
                                delete.setTextColor(color);
                                final Drawable bin = ContextCompat.getDrawable(_context,
                                    _deleteIconRes != -1 ? _deleteIconRes : R.drawable.ic_delete_24dp);
                                if (bin != null) {
                                    bin.setColorFilter(filter);
                                    delete.setCompoundDrawablesWithIntrinsicBounds(bin, null, null, null);
                                } else {
                                    delete.setCompoundDrawablesWithIntrinsicBounds(
                                        _deleteIconRes != -1 ? _deleteIconRes : R.drawable.ic_delete_24dp, 0, 0,
                                        0);
                                }
                                if(_enableDpad){
                                    delete.setBackgroundResource(R.drawable.listview_item_selector);
                                }
                                params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT,
                                    END | CENTER_VERTICAL);
                                params.rightMargin = 10;
                                options.addView(delete, params);

                                // Event Listeners.
                                createDir.setOnClickListener(new View.OnClickListener() {
                                    private EditText input = null;

                                    @Override
                                    public void onClick(final View view) {
                                        //Toast.makeText(getBaseContext(), "new folder clicked", Toast
                                        // .LENGTH_SHORT).show();
                                        hideOptions.run();
                                        File newFolder = new File(_currentDir, "New folder");
                                        for (int i = 1; newFolder.exists(); i++) {
                                            newFolder = new File(_currentDir, "New folder (" + i + ')');
                                        }
                                        if (this.input != null) {
                                            this.input.setText(newFolder.getName());
                                        }

                                        if (_newFolderView == null) {
                                            // region Draw a view with input to create new folder. (this only
                                            // happens the first time one clicks on New folder)
                                            try {
                                                //noinspection ConstantConditions
                                                ((AlertDialog) dialog).getWindow().clearFlags(
                                                    FLAG_NOT_FOCUSABLE | FLAG_ALT_FOCUSABLE_IM);
                                                //noinspection ConstantConditions
                                                ((AlertDialog) dialog).getWindow().setSoftInputMode(
                                                    SOFT_INPUT_STATE_VISIBLE);
                                            } catch (NullPointerException e) {
                                                e.printStackTrace();
                                            }

                                            // A semitransparent background overlay.
                                            final FrameLayout overlay = new FrameLayout(_context);
                                            overlay.setBackgroundColor(0x60ffffff);
                                            overlay.setScrollContainer(true);
                                            ViewGroup.MarginLayoutParams params = new FrameLayout.LayoutParams(
                                                MATCH_PARENT, MATCH_PARENT, CENTER);
                                            root.addView(overlay, params);

                                            overlay.setOnClickListener(null);
                                            overlay.setVisibility(View.INVISIBLE);
                                            _newFolderView = overlay;

                                            // A LinearLayout and a pair of Space to center views.
                                            LinearLayout linearLayout = new LinearLayout(_context);
                                            params = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT,
                                                CENTER);
                                            overlay.addView(linearLayout, params);

                                            Space leftSpace = new Space(_context);
                                            params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 2);
                                            linearLayout.addView(leftSpace, params);

                                            // A solid holder view for the EditText and Buttons.
                                            final LinearLayout holder = new LinearLayout(_context);
                                            holder.setOrientation(LinearLayout.VERTICAL);
                                            holder.setBackgroundColor(0xffffffff);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                holder.setElevation(20f);
                                            } else {
                                                ViewCompat.setElevation(holder, 20);
                                            }
                                            params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 5);
                                            linearLayout.addView(holder, params);

                                            Space rightSpace = new Space(_context);
                                            params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 2);
                                            linearLayout.addView(rightSpace, params);

                                            final EditText input = new EditText(_context);
                                            input.setText(newFolder.getName());
                                            input.setSelectAllOnFocus(true);
                                            input.setSingleLine(true);
                                            // There should be no suggestions, but...
                                            input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                                                | InputType.TYPE_TEXT_VARIATION_FILTER
                                                | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                                            input.setFilters(new InputFilter[]{
                                                _newFolderFilter != null ? _newFolderFilter
                                                    : new NewFolderFilter()});
                                            input.setGravity(CENTER_HORIZONTAL);
                                            params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                                            params.setMargins(3, 2, 3, 0);
                                            holder.addView(input, params);

                                            this.input = input;

                                            // A horizontal LinearLayout to hold buttons
                                            final FrameLayout buttons = new FrameLayout(_context);
                                            params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                                            holder.addView(buttons, params);

                                            // The Cancel button.
                                            final Button cancel = new Button(_context, null,
                                                android.R.attr.buttonBarButtonStyle);
                                            cancel.setText(_newFolderCancelRes);
                                            cancel.setTextColor(color);
                                            if(_enableDpad){
                                                cancel.setBackgroundResource(R.drawable.listview_item_selector);
                                            }
                                            params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT,
                                                START);
                                            buttons.addView(cancel, params);

                                            // The OK button.
                                            final Button ok = new Button(_context, null,
                                                android.R.attr.buttonBarButtonStyle);
                                            ok.setText(_newFolderOkRes);
                                            ok.setTextColor(color);
                                            if(_enableDpad){
                                                ok.setBackgroundResource(R.drawable.listview_item_selector);
                                            }
                                            params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT,
                                                END);
                                            buttons.addView(ok, params);

                                            // Event Listeners.
                                            input.setOnEditorActionListener(
                                                new TextView.OnEditorActionListener() {
                                                    @Override
                                                    public boolean onEditorAction(final TextView v,
                                                                                  final int actionId, final KeyEvent event) {
                                                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                                                            UiUtil.hideKeyboardFrom(_context, input);
                                                            if(!_enableDpad){
                                                                ChooserDialog.this.createNewDirectory(
                                                                    input.getText().toString());
                                                                overlay.setVisibility(View.INVISIBLE);
                                                                overlay.clearFocus();
                                                            } else{
                                                                input.requestFocus();
                                                            }
                                                            return true;
                                                        }
                                                        return false;
                                                    }
                                                });
                                            cancel.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(final View v) {
                                                    UiUtil.hideKeyboardFrom(_context, input);
                                                    overlay.setVisibility(View.INVISIBLE);
                                                    overlay.clearFocus();
                                                    if(_enableDpad){
                                                        _alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setFocusable(true);
                                                        _list.setFocusable(true);
                                                    }
                                                }
                                            });
                                            ok.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(final View v) {
                                                    ChooserDialog.this.createNewDirectory(
                                                        input.getText().toString());
                                                    UiUtil.hideKeyboardFrom(_context, input);
                                                    overlay.setVisibility(View.INVISIBLE);
                                                    overlay.clearFocus();
                                                    if(_enableDpad){
                                                        _alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setFocusable(true);
                                                        _list.setFocusable(true);
                                                    }
                                                }
                                            });
                                            // endregion
                                        }

                                        if (_newFolderView.getVisibility() == View.INVISIBLE) {
                                            _newFolderView.setVisibility(View.VISIBLE);
                                            if(_enableDpad){
                                                _newFolderView.requestFocus();
                                                _alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setFocusable(false);
                                                _list.setFocusable(false);
                                            }
                                        } else {
                                            _newFolderView.setVisibility(View.INVISIBLE);
                                            if(_enableDpad){
                                                _newFolderView.clearFocus();
                                                _alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setFocusable(true);
                                                _list.setFocusable(true);
                                            }
                                        }
                                    }
                                });
                                delete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(final View v1) {
                                        //Toast.makeText(_context, "delete clicked", Toast.LENGTH_SHORT).show();
                                        hideOptions.run();

                                        if (_chooseMode == CHOOSE_MODE_SELECT_MULTIPLE) {
                                            boolean success = true;
                                            for (File file : _adapter.getSelected()) {
                                                _result.onChoosePath(file.getAbsolutePath(), file);
                                                if (success) {
                                                    try {
                                                        deleteFile(file);
                                                    } catch (IOException e) {
                                                        Toast.makeText(_context, e.getMessage(), Toast.LENGTH_LONG).show();
                                                        success = false;
                                                    }
                                                }
                                            }
                                            _adapter.clearSelected();
                                            _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
                                            _chooseMode = CHOOSE_MODE_NORMAL;
                                            refreshDirs();
                                            return;
                                        }

                                        _chooseMode = _chooseMode != CHOOSE_MODE_DELETE ? CHOOSE_MODE_DELETE
                                            : CHOOSE_MODE_NORMAL;
                                    }
                                });
                                // endregion
                            }

                            if (_options.getVisibility() == View.VISIBLE) {
                                hideOptions.run();
                            } else {
                                showOptions.run();
                            }
                        }
                    });
                }
            }
        });
    }

    public ChooserDialog show() {
        //if (_result == null)
        //    throw new RuntimeException("no chosenListener defined. use withChosenListener() at first.");
        if (_alertDialog == null || _list == null) {
            throw new RuntimeException("call build() before show().");
        }

        // Check for permissions if SDK version is >= 23
        if (Build.VERSION.SDK_INT >= 23) {
            final int PERMISSION_REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE = 0;
            int readPermissionCheck = ContextCompat.checkSelfPermission(_context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermissionCheck = ContextCompat.checkSelfPermission(_context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

            //if = permission granted
            if (readPermissionCheck == PackageManager.PERMISSION_GRANTED
                && writePermissionCheck == PackageManager.PERMISSION_GRANTED) {
                _alertDialog.show();
            } else {
                ActivityCompat.requestPermissions((Activity) _context,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE);
                return this;
            }
        } else {
            _alertDialog.show();
        }

        if (_enableMultiple) {
            _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
        }

        if(_enableDpad){
            _alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setBackgroundResource(R.drawable.listview_item_selector);
            _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundResource(R.drawable.listview_item_selector);
            _alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundResource(R.drawable.listview_item_selector);
        }
        return this;
    }

    private void listDirs() {
        _entries.clear();

        // Get files
        File[] files = _currentDir.listFiles(_fileFilter);

        // Add the ".." entry
        boolean up = false;
        String removableRoot = FileUtil.getStoragePath(_context, true);
        if (removableRoot != null) {
            String primaryRoot = FileUtil.getStoragePath(_context, false);
            File f = Environment.getExternalStorageDirectory();
            //File newRoot = _currentDir.getParentFile();
            if (_currentDir.getAbsolutePath().equals(primaryRoot)) {
                _entries.add(new File(".. SDCard Storage") {
                    @Override
                    public boolean isDirectory() {
                        return true;
                    }

                    @Override
                    public boolean isHidden() {
                        return false;
                    }

                    @Override
                    public long lastModified() {
                        return 0L;
                    }
                }); //⇠
                up = true;
            } else {
                _entries.add(new File(".. Primary Storage") {
                    @Override
                    public boolean isDirectory() {
                        return true;
                    }

                    @Override
                    public boolean isHidden() {
                        return false;
                    }

                    @Override
                    public long lastModified() {
                        return 0L;
                    }
                }); //⇽
                up = true;
            }
        }
        if (!up && _currentDir.getParentFile() != null && _currentDir.getParentFile().canRead()) {
            _entries.add(new File("..") {
                @Override
                public boolean isHidden() {
                    return false;
                }

                @Override
                public long lastModified() {
                    return 0L;
                }
            });
        }

        if (files == null) return;

        List<File> dirList = new LinkedList<>();
        List<File> fileList = new LinkedList<>();

        for (File f : files) {
            if (f.isDirectory()) {
                dirList.add(f);
            } else {
                fileList.add(f);
            }
        }

        sortByName(dirList);
        sortByName(fileList);
        _entries.addAll(dirList);
        _entries.addAll(fileList);
    }

    private void sortByName(List<File> list) {
        Collections.sort(list, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });
    }

    private void listDirs2() {
        _entries.clear();

        // Get files
        File[] files = _currentDir.listFiles();

        // Add the ".." entry
        if (_currentDir.getParent() != null) {
            _entries.add(new File(".."));
        }

        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    continue;
                }

                _entries.add(file);
            }
        }

        sortByName(_entries);
    }


    private void createNewDirectory(String name) {
        final File newDir = new File(_currentDir, name);
        if (!newDir.exists() && newDir.mkdir()) {
            refreshDirs();
            return;
        }
        Toast.makeText(_context,
            "Couldn't create folder " + newDir.getName() + " at " + newDir.getAbsolutePath(),
            Toast.LENGTH_LONG).show();
    }

    private void deleteFile(File file) throws IOException {
        if (file.isDirectory()) {
            final File[] entries = file.listFiles();
            for (final File entry : entries) {
                deleteFile(entry);
            }
        }
        if (!file.delete()) {
            throw new IOException("Couldn't delete \"" + file.getName() + "\" at \"" + file.getParent());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent_, View list_, int position, long id_) {
        if (position < 0 || position >= _entries.size()) return;
        View focus = _list;

        boolean scrollToTop = false;
        File file = _entries.get(position);
        if (file.getName().equals("..")) {
            File f = _currentDir.getParentFile();
            if (_folderNavUpCB == null) _folderNavUpCB = _defaultNavUpCB;
            if (_folderNavUpCB.canUpTo(f)) {
                _currentDir = f;
                _chooseMode = _chooseMode == CHOOSE_MODE_DELETE ? CHOOSE_MODE_NORMAL : _chooseMode;
                scrollToTop = true;
            }
        } else if (file.getName().contains(".. SDCard Storage")){
            String removableRoot = FileUtil.getStoragePath(_context, true);
            if(removableRoot != null && Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState())){
                _currentDir = new File(removableRoot);
                _chooseMode = _chooseMode == CHOOSE_MODE_DELETE ? CHOOSE_MODE_NORMAL : _chooseMode;
            }
        } else if (file.getName().contains(".. Primary Storage")){
            String primaryRoot = FileUtil.getStoragePath(_context, false);
            if(primaryRoot != null){
                _currentDir = new File(primaryRoot);
                _chooseMode = _chooseMode == CHOOSE_MODE_DELETE ? CHOOSE_MODE_NORMAL : _chooseMode;
            }
        } else {
            switch (_chooseMode) {
                case CHOOSE_MODE_NORMAL:
                    if (file.isDirectory()) {
                        if (_folderNavToCB == null) _folderNavToCB = _defaultNavToCB;
                        if (_folderNavToCB.canNavigate(file)) {
                            _currentDir = file;
                            scrollToTop = true;
                        }
                    } else if ((!_dirOnly) && _result != null) {
                        _result.onChoosePath(file.getAbsolutePath(), file);
                        if (_enableMultiple) _result.onChoosePath(_currentDir.getAbsolutePath(), _currentDir);
                        if (_dismissOnButtonClick) _alertDialog.dismiss();
                        return;
                    }
                    break;
                case CHOOSE_MODE_SELECT_MULTIPLE:
                    if (file.isDirectory()) {
                        if (_folderNavToCB == null) _folderNavToCB = _defaultNavToCB;
                        if (_folderNavToCB.canNavigate(file)) {
                            _currentDir = file;
                            scrollToTop = true;
                        }
                    } else {
                        if(_enableDpad) focus = _alertDialog.getCurrentFocus();
                        _adapter.selectItem(position);
                        if (!_adapter.isAnySelected()) {
                            _chooseMode = CHOOSE_MODE_NORMAL;
                            _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
                        }
                        _result.onChoosePath(file.getAbsolutePath(), file);
                    }
                    break;
                case CHOOSE_MODE_DELETE:
                    try {
                        deleteFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(_context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    _chooseMode = CHOOSE_MODE_NORMAL;
                    break;
                default:
                    // ERROR! It shouldn't get here...
                    break;
            }
        }
        refreshDirs();
        if(scrollToTop) _list.setSelection(0);
        if(_enableDpad){
            if(focus == null) _list.requestFocus();
            else focus.requestFocus();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View list, int position, long id) {
        File file = _entries.get(position);
        if (file.getName().equals("..") || file.getName().contains(".. SDCard Storage") || file.getName().contains(".. Primary Storage") || file.isDirectory())
            return true;
        if (_adapter.isSelected(position)) return true;
        _result.onChoosePath(file.getAbsolutePath(), file);
        _adapter.selectItem(position);
        _chooseMode = CHOOSE_MODE_SELECT_MULTIPLE;
        _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
        return true;
    }

    private boolean lastSelected = false;
    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id){
        lastSelected = position == _entries.size() - 1;
    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent){
        //
    }

    @Override
    public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event){
        if(event.getAction() != KeyEvent.ACTION_DOWN) return false;
        if(lastSelected){
            if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                lastSelected = false;
                if(_options != null && _options.getVisibility() == View.VISIBLE){
                    _options.requestFocus();
                } else{
                    _alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).requestFocus();
                }
                return true;
            }
        } else if(_options != null && keyCode == KeyEvent.KEYCODE_DPAD_UP){
            if(_alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).hasFocus()
                || _alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).hasFocus()
                || _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).hasFocus()){
                if(_options.getVisibility() == View.VISIBLE){
                    _options.requestFocus();
                    return true;
                } else if(_options.isFocusable()){
                    _list.requestFocus();
                    lastSelected = true;
                    return true;
                }
            } else if(_options.hasFocus()){
                _list.requestFocus();
                lastSelected = true;
                return true;
            }
        }

        if(_alertDialog.getCurrentFocus() == _list) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                _onBackPressed.onBackPressed(_alertDialog);
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                onItemClick(null, _list, _list.getSelectedItemPosition(), _list.getSelectedItemId());
            }
        }
        return false;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        //
    }

    private void refreshDirs() {
        listDirs();
        _adapter.setEntries(_entries);
    }

    public void dismiss() {
        _alertDialog.dismiss();
    }

    private List<File> _entries = new ArrayList<>();
    private DirAdapter _adapter;
    private File _currentDir;
    private Context _context;
    private AlertDialog _alertDialog;
    private ListView _list;
    private Result _result = null;
    private boolean _dirOnly;
    private FileFilter _fileFilter;
    private @StringRes
    int _titleRes = R.string.choose_file, _okRes = R.string.title_choose, _negativeRes = R.string.dialog_cancel;
    private @DrawableRes
    int _iconRes = -1;
    private @LayoutRes
    int _layoutRes = -1;
    private @LayoutRes
    int _rowLayoutRes = -1;
    private String _dateFormat;
    private DialogInterface.OnClickListener _negativeListener;
    private DialogInterface.OnCancelListener _cancelListener2;
    private DialogInterface.OnDismissListener _onDismissListener;
    private boolean _disableTitle;
    private boolean _enableOptions;
    private View _options;
    private @StringRes
    int _createDirRes = R.string.option_create_folder, _deleteRes = R.string.options_delete,
        _newFolderCancelRes = R.string.new_folder_cancel, _newFolderOkRes = R.string.new_folder_ok;
    private @DrawableRes
    int _optionsIconRes = -1, _createDirIconRes = -1, _deleteIconRes = -1;
    private View _newFolderView;
    private boolean _dismissOnButtonClick = true;
    private boolean _enableMultiple;
    private boolean _enableDpad;

    @FunctionalInterface
    public interface AdapterSetter {
        void apply(DirAdapter adapter);
    }

    private AdapterSetter _adapterSetter = null;

    @FunctionalInterface
    public interface CanNavigateUp {
        boolean canUpTo(File dir);
    }

    @FunctionalInterface
    public interface CanNavigateTo {
        boolean canNavigate(File dir);
    }

    private CanNavigateUp _folderNavUpCB;
    private CanNavigateTo _folderNavToCB;

    private final static CanNavigateUp _defaultNavUpCB = new CanNavigateUp() {
        @Override
        public boolean canUpTo(File dir) {
            return dir != null && dir.canRead();
        }
    };

    private final static CanNavigateTo _defaultNavToCB = new CanNavigateTo() {
        @Override
        public boolean canNavigate(File dir) {
            return true;
        }
    };

    @FunctionalInterface
    public interface OnBackPressedListener {
        void onBackPressed(AlertDialog dialog);
    }

    private OnBackPressedListener _onBackPressed = (new OnBackPressedListener() {
        @Override
        public void onBackPressed(AlertDialog dialog) {
            if (_entries.size() > 0
                && (_entries.get(0).getName().equals("..")) ||
                _entries.get(0).getName().contains(".. SDCard Storage") ||
                _entries.get(0).getName().contains(".. Primary Storage")) {
                onItemClick(null, _list, 0, 0);
            } else {
                if (_onLastBackPressed != null) {
                    _onLastBackPressed.onBackPressed(dialog);
                } else {
                    _defaultLastBack.onBackPressed(dialog);
                }
            }
        }
    });
    private OnBackPressedListener _onLastBackPressed;

    private OnBackPressedListener _defaultLastBack = new OnBackPressedListener() {
        @Override
        public void onBackPressed(AlertDialog dialog) {
            dialog.dismiss();
        }
    };

    private static final int CHOOSE_MODE_NORMAL = 0;
    private static final int CHOOSE_MODE_DELETE = 1;
    private static final int CHOOSE_MODE_SELECT_MULTIPLE = 2;

    private int _chooseMode = CHOOSE_MODE_NORMAL;

    private NewFolderFilter _newFolderFilter;
}
