package com.obsez.android.lib.smbfilechooser.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.obsez.android.lib.smbfilechooser.FileChooserDialog;
import com.obsez.android.lib.smbfilechooser.SmbFileChooserDialog;
import com.obsez.android.lib.smbfilechooser.internals.FileUtil;
import com.obsez.android.lib.smbfilechooser.tool.DirAdapter;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

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
                        .build()
                        .show();
            }
        });

        final EditText mEditDomain = root.findViewById(R.id.edit_domain);
        final EditText mEditName = root.findViewById(R.id.edit_name);
        final EditText mEditPassword = root.findViewById(R.id.edit_password);
        root.findViewById(R.id.btn_choose_a_folder_smb).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View v){
                final Context ctx = getActivity();
                assert ctx != null;
                String domain = mEditDomain.getText().toString();
                if(domain.isEmpty()){
                    mEditDomain.setError("Required!");
                    mEditDomain.requestFocus();
                    return;
                }
                String name = mEditName.getText().toString();
                if(name.isEmpty()) name = null;
                String password = mEditPassword.getText().toString();
                if(password.isEmpty()) password = null;
                NtlmPasswordAuthentication auth;
                if(name == null && password == null) auth = null;
                    else auth = new NtlmPasswordAuthentication(domain, name, password);
                try{
                    SmbFileChooserDialog.newDialog(ctx, domain, auth)
                            .setResources(R.string.title_choose_folder_smb, R.string.title_choose, R.string.dialog_cancel)
                            .setFilter(true, false)
                            .enableOptions(true)
                            .setStartFile(null) // same as "smb://{domain}/
                            .setOnLastBackPressedListener(new SmbFileChooserDialog.OnBackPressedListener(){
                                @Override
                                public void onBackPressed(@NonNull final AlertDialog dialog){
                                    Toast.makeText(ctx, "This dialog won't close by pressing back!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNewFolderFilter(new FileUtil.NewFolderFilter(/*max length of 10*/ 10, /*regex pattern that only allows a to z (lowercase)*/ "[a-z]"))
                            .setOnChosenListener(new SmbFileChooserDialog.OnChosenListener(){
                                @Override
                                public void onChoosePath(@NonNull final String path, @NonNull final SmbFile file){
                                    Toast.makeText(ctx, "FOLDER: " + path, Toast.LENGTH_SHORT).show();
                                    _path = path;
                                    _tv.setText(_path);
                                }
                            })
                            .build()
                            .show();
                } catch(MalformedURLException | InterruptedException | ExecutionException e){
                    e.printStackTrace();
                    Toast.makeText(ctx, "Failed! try again", Toast.LENGTH_SHORT).show();
                }
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
                .enableOptions(true)
                .enableMultiple(true, true)
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
                .setOnSelectedListener(new FileChooserDialog.OnSelectedListener(){
                    @Override
                    public void onSelectFiles(@NonNull final List<File> files){
                        ArrayList<String> paths = new ArrayList<String>();
                        for (File file : files) {
                            paths.add(file.getAbsolutePath());
                        }

                        new AlertDialog.Builder(ctx)
                                .setTitle(files.size() + " files selected:")
                                .setAdapter(new ArrayAdapter<String>(ctx,
                                        android.R.layout.simple_expandable_list_item_1, paths),null)
                                .create()
                                .show();
                    }
                })
                .build()
                .show();
    }

}
