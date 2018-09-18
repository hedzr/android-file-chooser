package com.obsez.android.lib.smbfilechooser;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.obsez.android.lib.smbfilechooser.internals.ExtFileFilter;
import com.obsez.android.lib.smbfilechooser.internals.RegexFileFilter;
import com.obsez.android.lib.smbfilechooser.internals.UiUtil;
import com.obsez.android.lib.smbfilechooser.tool.DirAdapter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
import static com.obsez.android.lib.smbfilechooser.internals.FileUtil.LightContextWrapper;
import static com.obsez.android.lib.smbfilechooser.internals.FileUtil.NewFolderFilter;

//import android.support.v7.app.AlertDialog;

/**
 * Created by coco on 6/7/15. Edited by Guiorgy on 10/09/18.
 */
@SuppressWarnings("SpellCheckingInspection")
public class FileChooserDialog extends LightContextWrapper implements DialogInterface.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    @FunctionalInterface
    public interface OnChosenListener{
        void onChoosePath(@NonNull final String dir, @NonNull final File dirFile);
    }

    @FunctionalInterface
    public interface OnSelectedListener {
        void onSelectFiles(@NonNull final List<File> files);
    }

    private FileChooserDialog(@NonNull final Context context) {
        super(context);
    }

    @NonNull public static FileChooserDialog newDialog(@NonNull final Context context){
        return new FileChooserDialog(context);
    }

    @NonNull public FileChooserDialog setFilter(@NonNull final FileFilter ff) {
        setFilter(false, false, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    @NonNull public FileChooserDialog setFilter(final boolean dirOnly, final boolean allowHidden, @NonNull final FileFilter ff) {
        setFilter(dirOnly, allowHidden, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    @NonNull public FileChooserDialog setFilter(final boolean allowHidden, @Nullable final String... suffixes) {
        return setFilter(false, allowHidden, suffixes);
    }

    @NonNull public FileChooserDialog setFilter(final boolean dirOnly, final boolean allowHidden, @Nullable final String... suffixes) {
        this._dirOnly = dirOnly;
        if (suffixes == null) {
            this._fileFilter = dirOnly ? filterDirectoriesOnly : filterFiles;
        } else {
            this._fileFilter = new ExtFileFilter(_dirOnly, allowHidden, suffixes);
        }
        return this;
    }

    @NonNull public FileChooserDialog setFilterRegex(final boolean dirOnly, final boolean allowHidden, @NonNull final String pattern, final int flags) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, flags);
        return this;
    }

    @NonNull public FileChooserDialog setFilterRegex(final boolean dirOnly, final boolean allowHidden, @NonNull final String pattern) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, Pattern.CASE_INSENSITIVE);
        return this;
    }

    @NonNull public FileChooserDialog setStartFile(@Nullable final String startFile) {
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

    @NonNull public FileChooserDialog setCancelable(final boolean cancelable){
        this._cancelable = cancelable;
        return this;
    }

    @NonNull public FileChooserDialog cancelOnTouchOutside(final boolean cancelOnTouchOutside){
        this._cancelOnTouchOutside = cancelOnTouchOutside;
        return this;
    }

    @NonNull public FileChooserDialog dismissOnButtonClick(final boolean dismissOnButtonClick){
        this._dismissOnButtonClick = dismissOnButtonClick;
        if(dismissOnButtonClick){
            this._defaultLastBack = new OnBackPressedListener(){
                @Override
                public void onBackPressed(@NonNull final AlertDialog dialog){
                    dialog.dismiss();
                }
            };
        } else{
            this._defaultLastBack = new OnBackPressedListener(){
                @Override
                public void onBackPressed(@NonNull final AlertDialog dialog){
                    //
                }
            };
        }
        return this;
    }

    @NonNull public FileChooserDialog setOnChosenListener(@NonNull final OnChosenListener listener) {
        this._onChosenListener = listener;
        return this;
    }

    @NonNull public FileChooserDialog setOnSelectedListener(@NonNull final OnSelectedListener listener) {
        this._onSelectedListener = listener;
        return this;
    }

    @NonNull public FileChooserDialog setOnDismissListener(@NonNull final DialogInterface.OnDismissListener listener) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            this._onDismissListener = listener;
        }
        return this;
    }

    @NonNull public FileChooserDialog setOnBackPressedListener(@NonNull final OnBackPressedListener listener){
        this._onBackPressed = listener;
        return this;
    }

    @NonNull public FileChooserDialog setOnLastBackPressedListener(@NonNull final OnBackPressedListener listener){
        this._onLastBackPressed = listener;
        return this;
    }

    @NonNull public FileChooserDialog setResources(@StringRes final int titleRes, @StringRes final int okRes, @StringRes final int cancelRes) {
        this._titleRes = titleRes;
        this._okRes = okRes;
        this._negativeRes = cancelRes;
        return this;
    }

    @NonNull public FileChooserDialog setResources(@Nullable final String title, @Nullable final String ok, @Nullable final String cancel) {
        if(title != null){
            this._title = title;
            this._titleRes = -1;
        }
        if(ok != null){
            this._ok = ok;
            this._okRes = -1;
        }
        if(cancel != null){
            this._negative = cancel;
            this._negativeRes = -1;
        }
        return this;
    }

    @NonNull public FileChooserDialog enableOptions(final boolean enableOptions){
        this._enableOptions = enableOptions;
        return this;
    }

    @NonNull public FileChooserDialog setOptionResources(@StringRes final int createDirRes, @StringRes final int deleteRes, @StringRes final int newFolderCancelRes, @StringRes final int newFolderOkRes) {
        this._createDirRes = createDirRes;
        this._deleteRes = deleteRes;
        this._newFolderCancelRes = newFolderCancelRes;
        this._newFolderOkRes = newFolderOkRes;
        return this;
    }

    @NonNull public FileChooserDialog setOptionResources(@Nullable final String createDir, @Nullable final String delete, @Nullable final String newFolderCancel, @Nullable final String newFolderOk) {
        if(createDir != null){
            this._createDir = createDir;
            this._createDirRes = -1;
        }
        if(delete != null){
            this._delete = delete;
            this._deleteRes = -1;
        }
        if(newFolderCancel != null){
            this._newFolderCancel = newFolderCancel;
            this._newFolderCancelRes = -1;
        }
        if(newFolderOk != null){
            this._newFolderOk = newFolderOk;
            this._newFolderOkRes = -1;
        }
        return this;
    }

    @NonNull public FileChooserDialog setOptionIcons(@DrawableRes final int optionsIconRes, @DrawableRes final int createDirIconRes, @DrawableRes final int deleteRes) {
        this._optionsIconRes = optionsIconRes;
        this._createDirIconRes = createDirIconRes;
        this._deleteIconRes = deleteRes;
        return this;
    }

    @NonNull public FileChooserDialog setIcon(@DrawableRes final int iconId) {
        this._iconRes = iconId;
        return this;
    }

    @NonNull public FileChooserDialog setLayoutView(@LayoutRes final int layoutResId) {
        this._layoutRes = layoutResId;
        return this;
    }

    @NonNull public FileChooserDialog setRowLayoutView(@LayoutRes final int layoutResId) {
        this._rowLayoutRes = layoutResId;
        return this;
    }

    @NonNull public FileChooserDialog setDateFormat() {
        return this.setDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    @NonNull public FileChooserDialog setDateFormat(@NonNull final String format) {
        this._dateFormat = format;
        return this;
    }

    @NonNull public FileChooserDialog setNegativeButtonListener(@NonNull final DialogInterface.OnClickListener listener) {
        this._negativeListener = listener;
        return this;
    }

    /**
     * it's NOT recommended to use the `setOnCancelListener`, replace with `setNegativeButtonListener` pls.
     *
     * @deprecated will be removed at v1.2
     */
    @NonNull public FileChooserDialog setOnCancelListener(@NonNull final DialogInterface.OnCancelListener listener) {
        this._onCancelListener = listener;
        return this;
    }

    @NonNull public FileChooserDialog setFileIcons(final boolean tryResolveFileTypeAndIcon, @Nullable final Drawable fileIcon, @Nullable final Drawable folderIcon) {
        _adapterSetter = new AdapterSetter(){
            @Override
            public void apply(@NonNull final DirAdapter adapter){
                if(fileIcon != null)
                    adapter.setDefaultFileIcon(fileIcon);
                if(folderIcon != null)
                    adapter.setDefaultFolderIcon(folderIcon);
                adapter.setResolveFileType(tryResolveFileTypeAndIcon);
            }
        };
        return this;
    }

    @NonNull public FileChooserDialog setFileIconsRes(final boolean tryResolveFileTypeAndIcon, final int fileIcon, final int folderIcon) {
        _adapterSetter = new AdapterSetter(){
            @Override
            public void apply(@NonNull final DirAdapter adapter){
                if(fileIcon != -1){
                    adapter.setDefaultFileIcon(ContextCompat.getDrawable(FileChooserDialog.this.getBaseContext(), fileIcon));
                }
                if(folderIcon != -1){
                    adapter.setDefaultFolderIcon(
                            ContextCompat.getDrawable(FileChooserDialog.this.getBaseContext(), folderIcon));
                }
                adapter.setResolveFileType(tryResolveFileTypeAndIcon);
            }
        };
        return this;
    }

    /**
     * @param setter you can customize the folder navi-adapter set `setter`
     * @return this
     */
    @NonNull public FileChooserDialog setAdapterSetter(@NonNull final AdapterSetter setter) {
        _adapterSetter = setter;
        return this;
    }

    /**
     * @param cb give a hook at navigating up to a directory
     * @return this
     */
    @NonNull public FileChooserDialog setNavigateUpTo(@NonNull final CanNavigateUp cb) {
        _folderNavUpCB = cb;
        return this;
    }

    /**
     * @param cb give a hook at navigating to a child directory
     * @return this
     */
    @NonNull public FileChooserDialog setNavigateTo(@NonNull final CanNavigateTo cb) {
        _folderNavToCB = cb;
        return this;
    }

    @NonNull public FileChooserDialog disableTitle(final boolean b) {
        _disableTitle = b;
        return this;
    }

    @NonNull public FileChooserDialog enableMultiple(final boolean enableMultiple, final boolean allowSelectMultipleFolders){
        this._enableMultiple = enableMultiple;
        this._allowSelectDir = allowSelectMultipleFolders;
        return this;
    }

    @NonNull public FileChooserDialog setNewFolderFilter(@NonNull final NewFolderFilter filter){
        this._newFolderFilter = filter;
        return this;
    }

    @NonNull public FileChooserDialog build() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());

        this._adapter = new DirAdapter(getBaseContext(), new ArrayList<File>(),this. _rowLayoutRes != -1 ? this._rowLayoutRes : R.layout.li_row_textview, this._dateFormat);
        if (this._adapterSetter != null) {
            this._adapterSetter.apply(this._adapter);
        }
		refreshDirs();
        builder.setAdapter(this._adapter, this);

		if (!this._disableTitle){
            if(this._titleRes == -1) builder.setTitle(this._title);
              else builder.setTitle(this._titleRes);
        }

        if (this._iconRes != -1) {
            builder.setIcon(this._iconRes);
        }

        if (this._layoutRes != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setView(this._layoutRes);
            }
        }

        if (this._dirOnly || this._enableMultiple) {
            final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
                @Override
                public void onClick(final DialogInterface dialog, final int which){
                    if(FileChooserDialog.this._enableMultiple){
                        if(FileChooserDialog.this._adapter.isAnySelected()){
                            if(FileChooserDialog.this._adapter.isOneSelected()){
                                if(FileChooserDialog.this._onChosenListener != null){
                                    final File selected = _adapter.getSelected().get(0);
                                    FileChooserDialog.this._onChosenListener.onChoosePath(selected.getAbsolutePath(), selected);
                                }
                            } else{
                                if(FileChooserDialog.this._onSelectedListener != null){
                                    FileChooserDialog.this._onSelectedListener.onSelectFiles(_adapter.getSelected());
                                }
                            }
                        }
                    } else if(FileChooserDialog.this._onChosenListener != null){
                        FileChooserDialog.this._onChosenListener.onChoosePath(FileChooserDialog.this._currentDir.getAbsolutePath(), FileChooserDialog.this._currentDir);
                    }

                    FileChooserDialog.this._alertDialog.dismiss();
                }
            };

            if(this._okRes == -1) builder.setPositiveButton(this._ok, listener);
              else builder.setPositiveButton(this._okRes, listener);
        }

        final DialogInterface.OnClickListener listener = this._negativeListener != null ? this._negativeListener : new DialogInterface.OnClickListener(){
            @Override
            public void onClick(final DialogInterface dialog, final int which){
                dialog.cancel();
            }
        };

        if(this._negativeRes == -1) builder.setNegativeButton(this._negative, listener);
          else builder.setNegativeButton(this._negativeRes, listener);

        if (this._onCancelListener != null) {
            builder.setOnCancelListener(_onCancelListener);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            if(this._onDismissListener != null){
                builder.setOnDismissListener(this._onDismissListener);
            }
        }

        builder.setCancelable(this._cancelable)
               .setOnKeyListener(new DialogInterface.OnKeyListener(){
                   @Override
                   public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event){
                       if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                           if(FileChooserDialog.this._newFolderView != null && FileChooserDialog.this._newFolderView.getVisibility() == View.VISIBLE){
                               FileChooserDialog.this._newFolderView.setVisibility(View.INVISIBLE);
                               return true;
                           }

                           FileChooserDialog.this._onBackPressed.onBackPressed((AlertDialog) dialog);
                       }
                       return true;
                   }
               });

        this._alertDialog = builder.create();

        this._alertDialog.setCanceledOnTouchOutside(this._cancelOnTouchOutside);
        this._alertDialog.setOnShowListener(new DialogInterface.OnShowListener(){
            @Override
            public void onShow(final DialogInterface dialog){
                if(!FileChooserDialog.this._dismissOnButtonClick){
                    Button negative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    negative.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(final View v){
                            if(FileChooserDialog.this._negativeListener != null){
                                FileChooserDialog.this._negativeListener.onClick(FileChooserDialog.this._alertDialog, AlertDialog.BUTTON_NEGATIVE);
                            }
                        }
                    });

                    if(FileChooserDialog.this._dirOnly || FileChooserDialog.this._enableMultiple){
                        Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        positive.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(final View v){
                                if(FileChooserDialog.this._enableMultiple){
                                    if(FileChooserDialog.this._adapter.isAnySelected()){
                                        if(FileChooserDialog.this._adapter.isOneSelected()){
                                            if(FileChooserDialog.this._onChosenListener != null){
                                                final File selected = _adapter.getSelected().get(0);
                                                FileChooserDialog.this._onChosenListener.onChoosePath(selected.getAbsolutePath(), selected);
                                            }
                                        } else{
                                            if(FileChooserDialog.this._onSelectedListener != null){
                                                FileChooserDialog.this._onSelectedListener.onSelectFiles(_adapter.getSelected());
                                            }
                                        }
                                    }
                                } else if(FileChooserDialog.this._onChosenListener != null){
                                    FileChooserDialog.this._onChosenListener.onChoosePath(FileChooserDialog.this._currentDir.getAbsolutePath(), FileChooserDialog.this._currentDir);
                                }
                            }
                        });
                    }
                }

                if(FileChooserDialog.this._enableOptions){
                    final Button options = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                    options.setText("");
                    options.setVisibility(View.VISIBLE);
                    final Drawable drawable = ContextCompat.getDrawable(getBaseContext(),
                            FileChooserDialog.this._optionsIconRes != -1 ? FileChooserDialog.this._optionsIconRes : R.drawable.ic_menu_24dp);
                    final int color = UiUtil.getThemeAccentColor(getBaseContext());
                    final PorterDuffColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
                    if(drawable != null){
                        drawable.setColorFilter(filter);
                        options.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                    } else{
                        options.setCompoundDrawablesWithIntrinsicBounds(
                                FileChooserDialog.this._optionsIconRes != -1 ? FileChooserDialog.this._optionsIconRes : R.drawable.ic_menu_24dp, 0, 0, 0);
                    }

                    final Runnable showOptions = new Runnable(){
                        @Override
                        public void run(){
                            final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) FileChooserDialog.this._list.getLayoutParams();
                            if(FileChooserDialog.this._options.getHeight() == 0){
                                ViewTreeObserver viewTreeObserver = FileChooserDialog.this._options.getViewTreeObserver();
                                viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                                    @Override
                                    public boolean onPreDraw() {
                                        if (FileChooserDialog.this._options.getHeight() <= 0) { return false; }
                                        FileChooserDialog.this._options.getViewTreeObserver().removeOnPreDrawListener(this);
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable(){
                                            @Override
                                            public void run(){
                                                params.bottomMargin = _options.getHeight();
                                                FileChooserDialog.this._list.setLayoutParams(params);
                                                FileChooserDialog.this._options.setVisibility(View.VISIBLE);
                                            }
                                        }, 100); // Just to make sure that the View has been drawn, so the transition is smoother.
                                        return true;
                                    }
                                });
                            } else{
                                FileChooserDialog.this._options.setVisibility(View.VISIBLE);
                                params.bottomMargin = FileChooserDialog.this._options.getHeight();
                                FileChooserDialog.this._list.setLayoutParams(params);
                            }
                        }
                    };
                    final Runnable hideOptions = new Runnable(){
                        @Override
                        public void run(){
                            FileChooserDialog.this._options.setVisibility(View.INVISIBLE);
                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) FileChooserDialog.this._list.getLayoutParams();
                            params.bottomMargin = 0;
                            FileChooserDialog.this._list.setLayoutParams(params);
                        }
                    };

                    options.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(final View v){
                            if(FileChooserDialog.this._options == null){
                                // region Draw options view. (this only happens the first time one clicks on options)
                                // Root view (FrameLayout) of the ListView in the AlertDialog.
                                final int rootId = getResources().getIdentifier("contentPanel", "id", "android");
                                final FrameLayout root = ((AlertDialog) dialog).findViewById(rootId);
                                // In case the was changed or not found.
                                if(root == null) return;

                                // Create options view.
                                final FrameLayout options = new FrameLayout(getBaseContext());
                                //options.setBackgroundColor(0x60000000);
                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(MATCH_PARENT, 60, BOTTOM);
                                root.addView(options, params);

                                options.setOnClickListener(null);
                                options.setVisibility(View.INVISIBLE);
                                FileChooserDialog.this._options = options;

                                // Create a button for the option to create a new directory/folder.
                                final Button createDir = new Button(getBaseContext(), null, android.R.attr.buttonBarButtonStyle);
                                if(FileChooserDialog.this._createDirRes == -1) createDir.setText(FileChooserDialog.this._createDir);
                                  else createDir.setText(FileChooserDialog.this._createDirRes);
                                // Drawable for the button.
                                final Drawable plus = ContextCompat.getDrawable(getBaseContext(),
                                        FileChooserDialog.this._createDirIconRes != -1 ? FileChooserDialog.this._createDirIconRes : R.drawable.ic_add_24dp);
                                if(plus != null){
                                    plus.setColorFilter(filter);
                                    createDir.setCompoundDrawablesWithIntrinsicBounds(plus, null, null, null);
                                } else{
                                    createDir.setCompoundDrawablesWithIntrinsicBounds(
                                            FileChooserDialog.this._createDirIconRes != -1 ? FileChooserDialog.this._createDirIconRes : R.drawable.ic_add_24dp, 0, 0, 0);
                                }
                                params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, START | CENTER_VERTICAL);
                                params.leftMargin = 10;
                                options.addView(createDir, params);

                                // Create a button for the option to delete a file.
                                final Button delete = new Button(getBaseContext(), null, android.R.attr.buttonBarButtonStyle);
                                if(FileChooserDialog.this._deleteRes == -1) delete.setText(FileChooserDialog.this._delete);
                                  else delete.setText(FileChooserDialog.this._deleteRes);
                                final Drawable bin = ContextCompat.getDrawable(getBaseContext(),
                                        FileChooserDialog.this._deleteIconRes != -1 ? FileChooserDialog.this._deleteIconRes : R.drawable.ic_delete_24dp);
                                if(bin != null){
                                    bin.setColorFilter(filter);
                                    delete.setCompoundDrawablesWithIntrinsicBounds(bin, null, null, null);
                                } else{
                                    delete.setCompoundDrawablesWithIntrinsicBounds(
                                            FileChooserDialog.this._deleteIconRes != -1 ? FileChooserDialog.this._deleteIconRes : R.drawable.ic_delete_24dp, 0, 0, 0);
                                }
                                params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, END | CENTER_VERTICAL);
                                params.rightMargin = 10;
                                options.addView(delete, params);

                                // Event Listeners.
                                createDir.setOnClickListener(new View.OnClickListener(){
                                    private EditText input = null;

                                    @Override
                                    public void onClick(final View v1){
                                        //Toast.makeText(getBaseContext(), "new folder clicked", Toast.LENGTH_SHORT).show();
                                        hideOptions.run();
                                        File newFolder = new File(FileChooserDialog.this._currentDir, "New folder");
                                        for(int i = 1; newFolder.exists(); i++)
                                            newFolder = new File(FileChooserDialog.this._currentDir, "New Folder (" + i + ')');
                                        if(this.input != null) this.input.setText(newFolder.getName());

                                        if(FileChooserDialog.this._newFolderView == null){
                                            // region Draw a view with input to create new folder. (this only happens the first time one clicks on New folder)
                                            try{
                                                //noinspection ConstantConditions
                                                ((AlertDialog) dialog).getWindow().clearFlags(FLAG_NOT_FOCUSABLE  | FLAG_ALT_FOCUSABLE_IM);
                                                //noinspection ConstantConditions
                                                ((AlertDialog) dialog).getWindow().setSoftInputMode(SOFT_INPUT_STATE_VISIBLE);
                                            } catch(NullPointerException e){
                                                e.printStackTrace();
                                            }

                                            // A semitransparent background overlay.
                                            final FrameLayout overlay = new FrameLayout(getBaseContext());
                                            overlay.setBackgroundColor(0x60ffffff);
                                            overlay.setScrollContainer(true);
                                            ViewGroup.MarginLayoutParams params = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, CENTER);
                                            root.addView(overlay, params);

                                            overlay.setOnClickListener(null);
                                            overlay.setVisibility(View.INVISIBLE);
                                            FileChooserDialog.this._newFolderView = overlay;

                                            // A LynearLayout and a pair of Spaces to center vews.
                                            LinearLayout linearLayout = new LinearLayout(getBaseContext());
                                            params = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, CENTER);
                                            overlay.addView(linearLayout, params);

                                            // The Space on the left.
                                            Space leftSpace = new Space(getBaseContext());
                                            params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 2);
                                            linearLayout.addView(leftSpace, params);

                                            // A solid holder view for the EditText and Buttons.
                                            final LinearLayout holder = new LinearLayout(getBaseContext());
                                            holder.setOrientation(LinearLayout.VERTICAL);
                                            holder.setBackgroundColor(0xffffffff);
                                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                                                holder.setElevation(20f);
                                            } else{
                                                ViewCompat.setElevation(holder, 20);
                                            }
                                            params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 5);
                                            linearLayout.addView(holder, params);

                                            // The Space on the right.
                                            Space rightSpace = new Space(getBaseContext());
                                            params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 2);
                                            linearLayout.addView(rightSpace, params);

                                            final EditText input = new EditText(getBaseContext());
                                            input.setText(newFolder.getName());
                                            input.setSelectAllOnFocus(true);
                                            input.setSingleLine(true);
                                            // There should be no suggestions, but... android... :)
                                            input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_FILTER | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                                            input.setFilters(new InputFilter[] { FileChooserDialog.this._newFolderFilter != null ? FileChooserDialog.this._newFolderFilter : new NewFolderFilter() });
                                            input.setGravity(CENTER_HORIZONTAL);
                                            params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                                            params.setMargins(3, 2, 3, 0);
                                            holder.addView(input, params);

                                            this.input = input;

                                            // A horizontal LinearLayout to hold buttons
                                            final LinearLayout buttons = new LinearLayout(getBaseContext());
                                            buttons.setOrientation(LinearLayout.HORIZONTAL);
                                            params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                                            holder.addView(buttons, params);

                                            // The OK button.
                                            final Button cancel = new Button(getBaseContext(), null, android.R.attr.buttonBarButtonStyle);
                                            if(FileChooserDialog.this._newFolderCancelRes == -1) cancel.setText(FileChooserDialog.this._newFolderCancel);
                                              else cancel.setText(FileChooserDialog.this._newFolderCancelRes);
                                            cancel.setGravity(START);
                                            params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                                            buttons.addView(cancel, params);

                                            // The OK button.
                                            final Button ok = new Button(getBaseContext(), null, android.R.attr.buttonBarButtonStyle);
                                            if(FileChooserDialog.this._newFolderOkRes == -1) ok.setText(FileChooserDialog.this._newFolderOk);
                                              else ok.setText(FileChooserDialog.this._newFolderOkRes);
                                            ok.setGravity(END);
                                            params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                                            buttons.addView(ok, params);

                                            // Event Listeners.
                                            input.setOnEditorActionListener(new TextView.OnEditorActionListener(){
                                                @Override
                                                public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event){
                                                    if(actionId == EditorInfo.IME_ACTION_DONE){
                                                        FileChooserDialog.this.createNewDirectory(input.getText().toString());
                                                        UiUtil.hideKeyboardFrom(getBaseContext(), input);
                                                        overlay.setVisibility(View.INVISIBLE);
                                                        return true;
                                                    }
                                                    return false;
                                                }
                                            });
                                            cancel.setOnClickListener(new View.OnClickListener(){
                                                @Override
                                                public void onClick(final View v){
                                                    UiUtil.hideKeyboardFrom(getBaseContext(), input);
                                                    overlay.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                            ok.setOnClickListener(new View.OnClickListener(){
                                                @Override
                                                public void onClick(final View v){
                                                    FileChooserDialog.this.createNewDirectory(input.getText().toString());
                                                    UiUtil.hideKeyboardFrom(getBaseContext(), input);
                                                    overlay.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                            // endregion
                                        }

                                        if(FileChooserDialog.this._newFolderView.getVisibility() == View.INVISIBLE){
                                            FileChooserDialog.this._newFolderView.setVisibility(View.VISIBLE);
                                        } else{
                                            FileChooserDialog.this._newFolderView.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                                delete.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(final View v1){
                                        //Toast.makeText(getBaseContext(), "delete clicked", Toast.LENGTH_SHORT).show();
                                        hideOptions.run();

                                        if(FileChooserDialog.this._chooseMode == CHOOSE_MODE_SELECT_MULTIPLE){
                                            Queue<File> parents = new ArrayDeque<File>();
                                            File current = FileChooserDialog.this._currentDir.getParentFile();
                                            final File root = Environment.getExternalStorageDirectory();
                                            while(current != null && !current.equals(root)){
                                                parents.add(current);
                                                current = current.getParentFile();
                                            }

                                            for(File file : FileChooserDialog.this._adapter.getSelected()){
                                                try{
                                                    deleteFile(file);

                                                } catch(IOException e){
                                                    // There's probably a better way to handle this, but...
                                                    e.printStackTrace();
                                                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                    break;
                                                }
                                            }
                                            FileChooserDialog.this._adapter.clearSelected();

                                            // Check whether the current directory was deleted.
                                            if(!FileChooserDialog.this._currentDir.exists()){
                                                File parent;

                                                while((parent = parents.poll()) != null){
                                                    if(parent.exists()) break;
                                                }

                                                if(parent != null && parent.exists()){
                                                    FileChooserDialog.this._currentDir = parent;
                                                } else{
                                                    FileChooserDialog.this._currentDir = Environment.getExternalStorageDirectory();
                                                }
                                            }

                                            refreshDirs();
                                            FileChooserDialog.this._chooseMode = CHOOSE_MODE_NORMAL;
                                            return;
                                        }

                                        FileChooserDialog.this._chooseMode = FileChooserDialog.this._chooseMode != CHOOSE_MODE_DELETE ? CHOOSE_MODE_DELETE : CHOOSE_MODE_NORMAL;
                                    }
                                });
                                // endregion
                            }

                            if(FileChooserDialog.this._options.getVisibility() == View.VISIBLE){
                                hideOptions.run();
                            } else{
                                showOptions.run();
                            }
                        }
                    });
                }
            }
        });

        this._list = this._alertDialog.getListView();
        this._list.setOnItemClickListener(this);
        if (this._enableMultiple){
            this._list.setOnItemLongClickListener(this);
        }
        return this;
    }

    @NonNull public FileChooserDialog show() {
        if (_alertDialog == null || _list == null) {
            throw new RuntimeException("call build() before show().");
        }

        // Check for permissions if SDK version is >= 23
        if (Build.VERSION.SDK_INT >= 23) {
            final int PERMISSION_REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE = 0;

            final int readPermissionCheck = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            final int writePermissionCheck = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

            final int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;

            //if = permission granted
            if (readPermissionCheck == PERMISSION_GRANTED && writePermissionCheck == PERMISSION_GRANTED) {
                _alertDialog.show();
            } else {
                ActivityCompat.requestPermissions((Activity) getBaseContext(),
                        new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        PERMISSION_REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE);
                return this;
            }
        } else {
            _alertDialog.show();
        }

        if(_enableMultiple && !_dirOnly){
            _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
        }
        return this;
    }

    private void listDirs() {
        _entries.clear();

        // Get files
        File[] files = _currentDir.listFiles(_fileFilter);

        // Add the ".." entry
        if (_currentDir.getParentFile() != null && !_currentDir.getParent().equals("/storage/emulated")) {
            _entries.add(new File(".."));
        }

        if (files != null) {
            List<File> dirList = new LinkedList<>();
            for (File f : files) {
                if (f.isDirectory()) {
                    if (!f.getName().startsWith(".")) {
                        dirList.add(f);
                    }
                }
            }
            sortByName(dirList);
            _entries.addAll(dirList);

            List<File> fileList = new LinkedList<>();
            for (File f : files) {
                if (!f.isDirectory()) {
                    if (!f.getName().startsWith(".")) {
                        fileList.add(f);
                    }
                }
            }
            sortByName(fileList);
            _entries.addAll(fileList);
        }
    }

    void sortByName(@NonNull final List<File> list) {
        Collections.sort(list, new Comparator<File>(){
            @Override
            public int compare(final File f1, final File f2){
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });
    }

    /**
     * @deprecated better use listDirs as it sorts directories and files separately
     */
    @Deprecated
    private void listDirsUncategorised() {
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

    private void createNewDirectory(@NonNull final String name){
        final File newDir = new File(this._currentDir, name);
        if(!newDir.exists() && newDir.mkdir()){
            refreshDirs();
            return;
        }
        Toast.makeText(getBaseContext(),
                "Couldn't create folder " + newDir.getName() + " at " + newDir.getAbsolutePath(),
                Toast.LENGTH_LONG).show();
    }

    // todo: ask for confirmation! (inside an AlertDialog.. Ironical, I know)
    private void deleteFile(@NonNull final File file) throws IOException{
        if(file.isDirectory()){
            final File[] entries = file.listFiles();
            for(final File entry : entries){
                deleteFile(entry);
            }
        }
        if(!file.delete()) throw new IOException("Couldn't delete \"" + file.getName() + "\" at \"" + file.getParent());
    }

    @Override
    public void onItemClick(@Nullable final AdapterView<?> parent, @NonNull final View list, final int position, final long id) {
        if (position < 0 || position >= _entries.size()) return;

        File file = _entries.get(position);
        if (file.getName().equals("..")) {
            File f = _currentDir.getParentFile();
            if (_folderNavUpCB == null) _folderNavUpCB = _defaultNavUpCB;
            if (_folderNavUpCB.canUpTo(f)) _currentDir = f;
            if(_chooseMode == CHOOSE_MODE_DELETE) _chooseMode = CHOOSE_MODE_NORMAL;
        } else {
            switch(_chooseMode){
                case CHOOSE_MODE_NORMAL:
                    if (file.isDirectory()){
                        if (_folderNavToCB == null) _folderNavToCB = _defaultNavToCB;
                        if (_folderNavToCB.canNavigate(file)) _currentDir = file;
                    } else if ((!_dirOnly) && _onChosenListener != null){
                        _onChosenListener.onChoosePath(file.getAbsolutePath(), file);
                        if(_dismissOnButtonClick) _alertDialog.dismiss();
                        return;
                    }
                    break;
                case CHOOSE_MODE_SELECT_MULTIPLE:
                    if(file.isDirectory()){
                        if (_folderNavToCB == null) _folderNavToCB = _defaultNavToCB;
                        if (_folderNavToCB.canNavigate(file)) _currentDir = file;
                    } else{
                        _adapter.selectItem(position);
                        if(!_adapter.isAnySelected()){
                            _chooseMode = CHOOSE_MODE_NORMAL;
                            if(!_dirOnly) _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
                        }
                    }
                    break;
                case CHOOSE_MODE_DELETE:
                    try{
                        deleteFile(file);
                    } catch(IOException e){
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    _chooseMode = CHOOSE_MODE_NORMAL;
                    break;
                default:
                    // ERROR! It shouldn't get here...
                    break;
            }
        }
        refreshDirs();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View list, int position, long id) {
        File file = _entries.get(position);
        if (file.getName().equals("..")) return true;
        if(!_allowSelectDir && file.isDirectory()) return true;
        _adapter.selectItem(position);
        if(!_adapter.isAnySelected()){
            _chooseMode = CHOOSE_MODE_NORMAL;
            if(!_dirOnly) _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
        } else{
            _chooseMode = CHOOSE_MODE_SELECT_MULTIPLE;
            if(!_dirOnly) _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
        }
        return true;
    }

    @Override
    public void onClick(@NonNull final DialogInterface dialog, final int which) {
        //
    }

    private void refreshDirs() {
        listDirs();
		_adapter.setEntries(_entries);
    }

    public void dismiss(){
        if(_alertDialog == null) return;
        _alertDialog.dismiss();
    }

    public void cancel(){
        if(_alertDialog == null) return;
        _alertDialog.cancel();
    }

    private List<File> _entries = new ArrayList<>();
	private DirAdapter _adapter;
    private File _currentDir;
    private AlertDialog _alertDialog;
    private ListView _list;
    private OnChosenListener _onChosenListener = null;
    private OnSelectedListener _onSelectedListener = null;
    private boolean _dirOnly;
    private FileFilter _fileFilter;
    private @StringRes int _titleRes = -1, _okRes = -1, _negativeRes = -1;
    private @NonNull String _title = "Select a file", _ok = "Choose", _negative = "Cancel";
    private @DrawableRes int _iconRes = -1;
    private @LayoutRes int _layoutRes = -1;
    private @LayoutRes int _rowLayoutRes = -1;
    private String _dateFormat;
    private DialogInterface.OnClickListener _negativeListener;
    private DialogInterface.OnCancelListener _onCancelListener;
    private boolean _disableTitle;
    private boolean _cancelable = true;
    private boolean _cancelOnTouchOutside;
    private boolean _dismissOnButtonClick = true;
    private DialogInterface.OnDismissListener _onDismissListener;
    private boolean _enableOptions;
    private View _options;
    private @StringRes int _createDirRes = -1, _deleteRes = -1, _newFolderCancelRes = -1, _newFolderOkRes = -1;
    private @NonNull String _createDir = "New folder", _delete = "Delete", _newFolderCancel = "Cancel", _newFolderOk = "Ok";
    private @DrawableRes int _optionsIconRes = -1, _createDirIconRes = -1, _deleteIconRes = -1;
    private View _newFolderView;
    private boolean _enableMultiple;
    private boolean _allowSelectDir = false;

    @FunctionalInterface
    public interface AdapterSetter {
        void apply(@NonNull final DirAdapter adapter);
    }

    private AdapterSetter _adapterSetter = null;

    private final static FileFilter filterDirectoriesOnly = new FileFilter(){
        @Override
        public boolean accept(final File file){
            return file.isDirectory();
        }
    };

    private final static FileFilter filterFiles = new FileFilter(){
        @Override
        public boolean accept(final File file){
            return !file.isHidden();
        }
    };

    @FunctionalInterface
    public interface CanNavigateUp {
        boolean canUpTo(@Nullable final File dir);
    }

    @FunctionalInterface
    public interface CanNavigateTo {
        boolean canNavigate(@NonNull final File dir);
    }

    private CanNavigateUp _folderNavUpCB;
    private CanNavigateTo _folderNavToCB;

    private final static CanNavigateUp _defaultNavUpCB = new CanNavigateUp(){
        @Override
        public boolean canUpTo(@Nullable final File dir){
            return dir != null && dir.canRead();
        }
    };

    private final static CanNavigateTo _defaultNavToCB = new CanNavigateTo(){
        @Override
        public boolean canNavigate(@NonNull final File dir){
            return true;
        }
    };

    @FunctionalInterface
    public interface OnBackPressedListener{
        void onBackPressed(@NonNull final AlertDialog dialog);
    }

    private OnBackPressedListener _onBackPressed = (new OnBackPressedListener(){
        @Override
        public void onBackPressed(@NonNull final AlertDialog dialog){
            if(FileChooserDialog.this._entries.size() > 0
                    && (FileChooserDialog.this._entries.get(0).getName().equals("../") || FileChooserDialog.this._entries.get(0).getName().equals(".."))){
                FileChooserDialog.this.onItemClick(null, FileChooserDialog.this._list, 0, 0);
            } else{
                if(FileChooserDialog.this._onLastBackPressed != null) FileChooserDialog.this._onLastBackPressed.onBackPressed(dialog);
                  else FileChooserDialog.this._defaultLastBack.onBackPressed(dialog);
            }
        }
    });
    private OnBackPressedListener _onLastBackPressed;

    private OnBackPressedListener _defaultLastBack = new OnBackPressedListener(){
        @Override
        public void onBackPressed(@NonNull final AlertDialog dialog){
            dialog.dismiss();
        }
    };

    private static final int CHOOSE_MODE_NORMAL = 0;
    private static final int CHOOSE_MODE_DELETE = 1;
    private static final int CHOOSE_MODE_SELECT_MULTIPLE = 2;

    private int _chooseMode = CHOOSE_MODE_NORMAL;

    private NewFolderFilter _newFolderFilter;
}
