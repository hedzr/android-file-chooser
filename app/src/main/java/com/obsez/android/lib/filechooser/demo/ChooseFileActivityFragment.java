package com.obsez.android.lib.filechooser.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.ChooserDialog;
import com.obsez.android.lib.filechooser.internals.FileUtil;
import com.obsez.android.lib.filechooser.tool.DirAdapter;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;


/**
 * A placeholder fragment containing a simple view.
 */
public class ChooseFileActivityFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ChooseFileActivityFragment";
    private String _path;
    private TextView _tv;
    private ImageView _iv;

    public ChooseFileActivityFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String ext = FileUtil.getStoragePath(getActivity(), true);
        String itl = FileUtil.getStoragePath(getActivity(), false);
        Timber.v("ext: " + ext + ", total size: " + FileUtil.getReadableFileSize(FileUtil.readSDCard(getActivity(), true)) + ", free size: " + FileUtil.getReadableFileSize(FileUtil.readSDCard(getActivity(), true, true)));
        Timber.v("itl: " + itl + ", total size: " + FileUtil.getReadableFileSize(FileUtil.readSDCard(getActivity(), false)) + ", free size: " + FileUtil.getReadableFileSize(FileUtil.readSDCard(getActivity(), false, true)));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_choose_file, container, false);
        _tv = (TextView) root.findViewById(R.id.textView);
        _tv.setText(BuildConfig.VERSION_NAME);
        _iv = (ImageView) root.findViewById(R.id.imageView);
        root.findViewById(R.id.btn_choose_a_file).setOnClickListener(this);
        root.findViewById(R.id.btn_choose_a_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // choose a folder
                final Context ctx = getActivity();
                assert ctx != null;
                new ChooserDialog().with(ctx)
                    .withIcon(R.mipmap.ic_launcher)
                    .withFilter(true, false)
                    .withStartFile(_path)
                    .withDateFormat("HH:mm")
                    .withResources(R.string.title_choose_folder, R.string.title_choose, R.string.dialog_cancel)
                    //.withNavigateUpTo(new ChooserDialog.CanNavigateUp() {
                    //    @Override
                    //    public boolean canUpTo(File dir) {
                    //        return true;
                    //    }
                    //})
                    .withOnCancelListener(new DialogInterface.OnCancelListener() {

                        /**
                         * This method will be invoked when the dialog is canceled.
                         *
                         * @param dialog the dialog that was canceled will be passed into the
                         *               method
                         */
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Log.d("CANCEL", "CANCEL");
                        }
                    })
                    .withNegativeButtonListener(new DialogInterface.OnClickListener() {

                        /**
                         * This method will be invoked when a button in the dialog is clicked.
                         *
                         * @param dialog the dialog that received the click
                         * @param which  the button that was clicked (ex.
                         *               {@link DialogInterface#BUTTON_POSITIVE}) or the position
                         */
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("Negative", "Negative");
                        }
                    })
                    .withChosenListener(new ChooserDialog.Result() {
                        @Override
                        public void onChoosePath(String path, File pathFile) {
                            Toast.makeText(ctx, "FOLDER: " + path, Toast.LENGTH_SHORT).show();
                            _path = path;
                            _tv.setText(_path);
                        }
                    })
                    .enableOptions(true)
                    .withOnBackPressedListener(new ChooserDialog.OnBackPressedListener() {
                        @Override
                        public void onBackPressed(AlertDialog dialog) {
                            Log.d("backpressed", "back pressed");
                            dialog.dismiss();
                        }
                    })
                    .build()
                    .show();
            }
        });
        root.findViewById(R.id.btn_choose_any_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context ctx = getActivity();
                assert ctx != null;
                new ChooserDialog(ctx)
                    .disableTitle(true)
                    .withStartFile(_path)
                    .withResources(R.string.title_choose_any_file, R.string.title_choose, R.string.dialog_cancel)
                    .withFileIconsRes(false, R.mipmap.ic_my_file, R.mipmap.ic_my_folder)
                    .withAdapterSetter(new ChooserDialog.AdapterSetter() {
                        @Override
                        public void apply(DirAdapter adapter) {
                            //
                        }
                    })
                    .withChosenListener(new ChooserDialog.Result() {
                        @Override
                        public void onChoosePath(String path, File pathFile) {
                            Toast.makeText(ctx, "FILE: " + path, Toast.LENGTH_SHORT).show();

                            _path = path;
                            _tv.setText(_path);
                            //_iv.setImageURI(Uri.fromFile(pathFile));
                            _iv.setImageBitmap(ImageUtil.decodeFile(pathFile));
                        }
                    })
                    .withOnBackPressedListener(new ChooserDialog.OnBackPressedListener() {
                        @Override
                        public void onBackPressed(AlertDialog dialog) {
                            dialog.dismiss();
                        }
                    })
                    .build()
                    .show();
            }
        });
        root.findViewById(R.id.btn_choose_multiple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<File> files = new ArrayList<File>();
                final Context ctx = getActivity();
                assert ctx != null;
                final ChooserDialog dialog = new ChooserDialog(ctx);
                dialog.enableOptions(true)
                    .withFilter(false, false)
                    .withStartFile(_path)
                    .withResources(R.string.title_choose_multiple, R.string.new_folder_ok,
                        R.string.dialog_cancel)
                    .enableMultiple(true)
                    .dismissOnButtonClick(false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    dialog.withOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (files.isEmpty()) return;

                            ArrayList<String> paths = new ArrayList<String>();
                            for (File file : files) {
                                paths.add(file.getAbsolutePath());
                            }

                            new AlertDialog.Builder(ctx)
                                .setTitle(files.size() + " files selected:")
                                .setAdapter(new ArrayAdapter<String>(ctx,
                                    android.R.layout.simple_expandable_list_item_1, paths), null)
                                .create()
                                .show();
                        }
                    })
                        .withOnLastBackPressedListener(new ChooserDialog.OnBackPressedListener() {
                            @Override
                            public void onBackPressed(AlertDialog dialog) {
                                files.clear();
                                dialog.dismiss();
                            }
                        })
                        .withNegativeButtonListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                files.clear();
                                dialog.dismiss();
                            }
                        })
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String dir, File dirFile) {
                                if (dirFile.isDirectory()) {
                                    dialog.dismiss();
                                    return;
                                }
                                if (!files.remove(dirFile)) {
                                    files.add(dirFile);
                                }
                            }
                        });
                } else {
                    // OnDismissListener is not supported, so we simulate something similar anywhere where the
                    // dialog might be dismissed.
                    final Runnable onDismiss = new Runnable() {
                        @Override
                        public void run() {
                            if (files.isEmpty()) return;

                            ArrayList<String> paths = new ArrayList<String>();
                            for (File file : files) {
                                paths.add(file.getAbsolutePath());
                            }

                            new AlertDialog.Builder(ctx)
                                .setTitle(files.size() + " files selected:")
                                .setAdapter(new ArrayAdapter<String>(ctx,
                                    android.R.layout.simple_expandable_list_item_1, paths), null)
                                .create()
                                .show();
                        }
                    };

                    dialog
                        .withOnLastBackPressedListener(new ChooserDialog.OnBackPressedListener() {
                            @Override
                            public void onBackPressed(AlertDialog dialog) {
                                files.clear();
                                dialog.dismiss();
                                onDismiss.run();
                            }
                        })
                        .withNegativeButtonListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                files.clear();
                                dialog.dismiss();
                                onDismiss.run();
                            }
                        })
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String dir, File dirFile) {
                                if (dirFile.isDirectory()) {
                                    dialog.dismiss();
                                    onDismiss.run();
                                    return;
                                }
                                if (!files.remove(dirFile)) {
                                    files.add(dirFile);
                                }
                            }
                        });
                }

                dialog.build().show();
            }
        });
        return root;
    }

    @Override
    public void onClick(View v) {
        //choose a file
        final Context ctx = this.getActivity();
        assert ctx != null;
        //Demo.INSTANCE.demo1(ctx, _path, new ChooserDialog.Result() {
        //    @Override
        //    public void onChoosePath(String path, File pathFile) {
        //        Toast.makeText(ctx, "FILE: " + path, Toast.LENGTH_SHORT).show();
        //
        //        _path = path;
        //        _tv.setText(_path);
        //        //_iv.setImageURI(Uri.fromFile(pathFile));
        //        _iv.setImageBitmap(ImageUtil.decodeFile(pathFile));
        //    }
        //});

        new ChooserDialog(ctx)
            .withFilterRegex(false, true, ".*\\.(jpe?g|png)")
            .withStartFile(_path)
            .withResources(R.string.title_nothing, R.string.title_choose, R.string.dialog_cancel)
            .withChosenListener(new ChooserDialog.Result() {
                @Override
                public void onChoosePath(String path, File pathFile) {
                    Toast.makeText(ctx, "FILE: " + path, Toast.LENGTH_SHORT).show();

                    _path = path;
                    _tv.setText(_path);
                    //_iv.setImageURI(Uri.fromFile(pathFile));
                    _iv.setImageBitmap(ImageUtil.decodeFile(pathFile));
                }
            })
            .withOnBackPressedListener(new ChooserDialog.OnBackPressedListener() {
                @Override
                public void onBackPressed(AlertDialog dialog) {
                    dialog.dismiss();
                }
            })
            //.enableOptions(true)
            .build()
            .show();
    }

}
