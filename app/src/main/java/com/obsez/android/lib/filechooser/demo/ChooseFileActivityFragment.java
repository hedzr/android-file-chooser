package com.obsez.android.lib.smbfilechooser.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.obsez.android.lib.smbfilechooser.FileChooserDialog;
import com.obsez.android.lib.smbfilechooser.internals.FileUtil;
import com.obsez.android.lib.smbfilechooser.tool.DirAdapter;

import java.io.File;

//import android.support.v7.app.AlertDialog;


/**
 * A placeholder fragment containing a simple view.
 */
@SuppressWarnings("RedundantCast")
public class ChooseFileActivityFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ChooseFileActivityFragment";
    private String _path;
    private TextView _tv;
    private ImageView _iv;

    public ChooseFileActivityFragment() {
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
                FileChooserDialog.newDialog(ctx)
                        .setIcon(R.mipmap.ic_launcher)
                        .setFilter(true, false)
                        .setStartFile(_path)
                        .setDateFormat("HH:mm")
                        .setResources(R.string.title_choose_folder, R.string.title_choose, R.string.dialog_cancel)
                        //.setOnCancelListener(new DialogInterface.OnCancelListener(){
                        //
                        //    /**
                        //     * This method will be invoked when the dialog is canceled.
                        //     *
                        //     * @param dialog the dialog that was canceled will be passed into the method
                        //     */
                        //    @Override
                        //    public void onCancel(DialogInterface dialog) {
                        //        Log.d("CANCEL", "CANCEL");
                        //    }
                        //})
                        //.setNegativeButtonListener(new DialogInterface.OnClickListener(){
                        //
                        //    /**
                        //     * This method will be invoked when the cancel button in the dialog is clicked.
                        //     *
                        //     * @param dialog the dialog that received the click
                        //     * @param which the button that was clicked (ex. {@link DialogInterface#BUTTON_NEGATIVE}) or the position
                        //     */
                        //    @Override
                        //    public void onClick(DialogInterface dialog, int which) {
                        //        Log.d("Negative", "Negative");
                        //    }
                        //})
                        //.setOnDismissListener(new DialogInterface.OnDismissListener(){
                        //
                        //    /**
                        //     * This method will be invoked when the dialog is dismissed. (both when canceled or file chosen)
                        //     * OnDismissListener is only available on API 17 or higher!
                        //     *
                        //     * @param dialog the dialog that was dismissed
                        //     */
                        //    @Override
                        //    public void onDismiss(final DialogInterface dialog){
                        //        Log.d("DISMISS", "DISMISS");
                        //    }
                        //})
                        .setOnChosenListener(new FileChooserDialog.OnChosenListener() {
                            @Override
                            public void onChoosePath(@NonNull String path, @NonNull File pathFile) {
                                Toast.makeText(ctx, "FOLDER: " + path, Toast.LENGTH_SHORT).show();
                                _path = path;
                                _tv.setText(_path);
                            }
                        })
                        .cancelOnTouchOutside(true)
                        .enableOptions(true)
                        .setOnLastBackPressedListener(new FileChooserDialog.OnBackPressedListener(){
                            @Override
                            public void onBackPressed(@NonNull final AlertDialog dialog){
                                Toast.makeText(ctx, "there is no parent directory", Toast.LENGTH_SHORT).show();
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
                FileChooserDialog.newDialog(ctx)
                        .disableTitle(true)
                        .setStartFile(_path)
                        .setResources(R.string.title_choose_any_file, R.string.title_choose,
                                R.string.dialog_cancel)
                        .setFileIconsRes(false, R.mipmap.ic_my_file, R.mipmap.ic_my_folder)
                        .setAdapterSetter(new FileChooserDialog.AdapterSetter() {
                            @Override
                            public void apply(@NonNull DirAdapter adapter) {
                                //
                            }
                        })
                        .setOnChosenListener(new FileChooserDialog.OnChosenListener() {
                            @Override
                            public void onChoosePath(@NonNull String path, @NonNull File pathFile) {
                                Toast.makeText(ctx, "FILE: " + path, Toast.LENGTH_SHORT).show();

                                _path = path;
                                _tv.setText(_path);
                                //_iv.setImageURI(Uri.fromFile(pathFile));
                                _iv.setImageBitmap(ImageUtil.decodeFile(pathFile));
                            }
                        })
                        .enableOptions(true)
                        .build()
                        .show();
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

        FileChooserDialog.newDialog(ctx)
                .setFilterRegex(false, true, ".*\\.(jpe?g|png)")
                .setStartFile(_path)
                .setResources(R.string.title_nothing, R.string.title_choose, R.string.dialog_cancel)
                .setOnChosenListener(new FileChooserDialog.OnChosenListener() {
                    @Override
                    public void onChoosePath(@NonNull String path, @NonNull File pathFile) {
                        Toast.makeText(ctx, "FILE: " + path, Toast.LENGTH_SHORT).show();

                        _path = path;
                        _tv.setText(_path);
                        //_iv.setImageURI(Uri.fromFile(pathFile));
                        _iv.setImageBitmap(ImageUtil.decodeFile(pathFile));
                    }
                })
                .enableOptions(true)
                .build()
                .show();
    }

}
