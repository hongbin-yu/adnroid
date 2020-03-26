package dajana.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dajana.adapter.RecentImagesAdapter;
import wang.switchy.hin2n.R;

public class SearchDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    private TextView attachSendButtonText;
    private TextView searchKeywordText;
    private ImageView attachSendButtonIcon;

    private Listener listener;

    public interface Listener {
        void onSearchSend(String keywords);
        void onPhotoClick();
        void onDocumentClick();
        void onVideoClick();
        void onAudioClick();
    }

    public static SearchDialog newInstance(Listener listener) {
        SearchDialog dialog = new SearchDialog();
        dialog.setListener(listener);
        return dialog;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.search_dialog, container, false);

        view.findViewById(R.id.search_send_button).setOnClickListener(this);
        view.findViewById(R.id.search_video_button).setOnClickListener(this);
        view.findViewById(R.id.search_photo_button).setOnClickListener(this);
        view.findViewById(R.id.search_audio_button).setOnClickListener(this);
        view.findViewById(R.id.search_document_button).setOnClickListener(this);

        searchKeywordText = view.findViewById(R.id.search_keywords_text);
        attachSendButtonText = view.findViewById(R.id.search_send_button_text_view);
        attachSendButtonText.setVisibility(View.INVISIBLE);
        attachSendButtonIcon = view.findViewById(R.id.search_send_button_icon);

        //RecyclerView recyclerView = view.findViewById(R.id.attach_recent_images_recycler_view);
        //recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        searchKeywordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    attachSendButtonText.setVisibility(View.VISIBLE);
                    attachSendButtonText.setText(String.format(Locale.getDefault(),"Send (%d)", charSequence.length()));
                    attachSendButtonIcon.setImageResource(R.drawable.ic_send_circle);
                } else {
                    attachSendButtonText.setVisibility(View.INVISIBLE);
                    attachSendButtonIcon.setImageResource(R.drawable.ic_down_circle);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_send_button:
                listener.onSearchSend(searchKeywordText.getText().toString());
                break;
            case R.id.search_video_button:
                listener.onVideoClick();
                break;
            case R.id.search_photo_button:
                listener.onPhotoClick();
                break;
            case R.id.search_document_button:
                listener.onDocumentClick();
                break;
            case R.id.search_audio_button:
                listener.onAudioClick();
                break;
        }

        dismiss();
    }

}