package dajana.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import dajana.utils.FileCategory;
import wang.switchy.hin2n.BuildConfig;
import wang.switchy.hin2n.R;

import static dajana.utils.FileCategory.getFileIconByCategory;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImageViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ImageViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageViewFragment extends Fragment {
    private static final String IMAGE_PATH = "IMAGE_PATH";
    private static final String IMAGE_URL = "IMAGE_URL";
    private static final String IMAGE_CONTENTTYPE = "CONTENT_TYPE";
    private static final String ATTACHMENT_ID = "ATTACHMENT_ID";
    private static final String LOG_TAG = "ImageViewFragment";
    private ImageView ivPhoto;
    private ProgressBar progressBar;

    public static ImageViewFragment newInstance(String imagePath, String imageUrl, String contentType, Integer attachmentId) {
        ImageViewFragment fragment = new ImageViewFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_PATH, imagePath);
        args.putString(IMAGE_CONTENTTYPE, contentType);
        args.putString(IMAGE_URL, imageUrl);
        args.putString(ATTACHMENT_ID, attachmentId.toString());
        fragment.setArguments(args);
        return fragment;
    }


    //private OnFragmentInteractionListener mListener;

    public ImageViewFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.onViewCreated(view, savedInstanceState);

        // get params
        Bundle args = getArguments();
        if (args == null) return;

        String source;
        String uri = args.getString(IMAGE_URL);
        final String path = args.getString(IMAGE_PATH);
        final String contentType = args.getString(IMAGE_CONTENTTYPE);
        if (path != null && new File(path).exists()) source = path;
        else source = uri;
        final String attachmentId = args.getString(ATTACHMENT_ID);

        // find views
        ivPhoto = view.findViewById(R.id.ivPhoto);
        progressBar = view.findViewById(R.id.progressBar);

        // setup image
        if(contentType != null && contentType.startsWith("image")) {
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(getActivity()).load(source)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            if (e != null) showError(e.toString());
                            //if (path != null) MessageManager.setAttachmentLocalPathToNull(attachmentId);
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }


                    })
                    .into(ivPhoto);
        }else if(contentType != null && contentType.startsWith("video") && source.equals(path)) {
            Bitmap bmThumbnail;
            bmThumbnail = ThumbnailUtils.createVideoThumbnail(source, MediaStore.Video.Thumbnails.MINI_KIND);
            ivPhoto.setImageBitmap(bmThumbnail);
        }else {
            ivPhoto.setImageResource(getFileIconByCategory(FileCategory.determineFileCategory(contentType)));
            ivPhoto.setOnClickListener(new View.OnClickListener(){


                @Override
                public void onClick(View view) {
                    openFile(contentType,path);
                }
            });
        }

    }
/*
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
*/
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    */
    public void showError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void openFile(String contentType, String path) {
        try {
            File data = new File(path);
            Uri uri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileProvider", new File(path));
            if (contentType == null) {
                Toast.makeText(getActivity(), "ContentType is null", Toast.LENGTH_SHORT).show();
                return;
            }
            if (uri == null) {
                Toast.makeText(getActivity(), "Uri is null", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, contentType);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, e.getMessage());
            Toast.makeText(getActivity(), R.string.toast_could_not_open_file, Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, e.getMessage());
            Toast.makeText(getActivity(), "Null exception," + R.string.toast_could_not_open_file, Toast.LENGTH_SHORT).show();
        }
    }

}
