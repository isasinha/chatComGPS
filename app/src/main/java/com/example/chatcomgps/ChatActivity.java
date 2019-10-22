package com.example.chatcomgps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mensagensRecyclerView;
    private ChatAdapter adapter;
    private List<com.example.chatcomgps.Mensagem> mensagens;
    private FirebaseUser fireUser;
    private CollectionReference collMensagensReference;
    private EditText mensagemEditText;
    private ImageView sendedImageView;
    private static final int REQ_CODE_CAMERA = 1001;
    private Context context;
    public static boolean hasFoto = false;
    public static Bitmap picture;
    private View view;

    private void setupFirebase (){
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        collMensagensReference = FirebaseFirestore.getInstance().collection("mensagens");
        collMensagensReference.addSnapshotListener( (result, e) -> {
            mensagens.clear();
            for (DocumentSnapshot doc: result.getDocuments()){
                com.example.chatcomgps.Mensagem m = doc.toObject(com.example.chatcomgps.Mensagem.class);
                mensagens.add(m);
            }
            Collections.sort(mensagens);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupFirebase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mensagemEditText = findViewById(R.id.mensagemEditText);
        mensagensRecyclerView = findViewById(R.id.mensagensRecyclerView);
        mensagens = new ArrayList<>();
        adapter = new ChatAdapter(this, mensagens);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mensagensRecyclerView.setAdapter(adapter);
        mensagensRecyclerView.setLayoutManager(llm);
    }
    public void enviarLocalizacao(View view) {
        Intent intent = new Intent(this, com.example.chatcomgps.GpsActivity.class);
        startActivity(intent);
    }

    public void enviarMensagem(View view){

        String texto = mensagemEditText.getText().toString();
        com.example.chatcomgps.Mensagem m = new com.example.chatcomgps.Mensagem( texto, new java.util.Date(), fireUser.getEmail());
        collMensagensReference.add(m);
        mensagemEditText.setText("");
    }

    public void tirarFoto(View view){
        this.view = view;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //dá pra tirar foto?
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQ_CODE_CAMERA);
        }
        else
            Toast.makeText(this, getString(R.string.cant_take_pic), Toast.LENGTH_SHORT).show();

    }

    private void uploadPicture (Bitmap picture){
        String currentUser = fireUser.getEmail();
        StorageReference pictureStorageReference = FirebaseStorage.getInstance().getReference(
                String.format(
                        Locale.getDefault(),
                        "images/%s/sendedPic.jpg",
                        currentUser.replace("@", ""))
        );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte [] bytes = baos.toByteArray();
        //aqui foi feito o upload
        pictureStorageReference.putBytes(bytes);
        hasFoto = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        sendedImageView = findViewById(R.id.sendedImageView);
        if(requestCode == REQ_CODE_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                picture = (Bitmap)data.getExtras().get("data");
                uploadPicture(picture);
//                sendedImageView.setImageBitmap(picture);
                enviarMensagem(view);
//                Bitmap imagem = ((BitmapDrawable) sendedImageView.getDrawable()).getBitmap();
//                Mensagem m = new Mensagem(new java.util.Date(), fireUser.getEmail(), picture);
//                collMensagensReference.add(m);
            }
            else {
                Toast.makeText(this, getString(R.string.no_pic_taken), Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}


class ChatViewHolder extends RecyclerView.ViewHolder{
    public TextView dataNomeTextView;
    public TextView mensagemTextView;
    public ImageView profilePicImageView;
    public ImageView sendedImageView;
    public ChatViewHolder (View raiz){
        super(raiz);
        dataNomeTextView = raiz.findViewById(R.id.dataNomeTextView);
        mensagemTextView = raiz.findViewById(R.id.mensagemTextView);
        profilePicImageView = raiz.findViewById(R.id.profilePicImageView);
        sendedImageView = raiz.findViewById(R.id.sendedImageView);
    }
}

class ChatAdapter extends RecyclerView.Adapter <ChatViewHolder>{

    private Context context;
    private List<com.example.chatcomgps.Mensagem> mensagens;
    private Map<String, Bitmap> fotos;
    private Map<String, Bitmap> fotoSended;
    private ChatActivity chat = new ChatActivity();

    public ChatAdapter(Context context, List<com.example.chatcomgps.Mensagem> mensagens){
        this.context = context;
        this.mensagens = mensagens;
        fotos = new HashMap<>();
        fotoSended = new HashMap<>();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //return null;
        LayoutInflater inflater = LayoutInflater.from(context);
        View raiz = inflater.inflate(
                R.layout.list_item,
                parent,
                false
        );
        return new ChatViewHolder(raiz);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        com.example.chatcomgps.Mensagem m = mensagens.get(position);
        holder.dataNomeTextView.setText(context.getString(R.string.data_nome, com.example.chatcomgps.DateHelper.format(m.getData()), m.getEmail()));
        holder.mensagemTextView.setText(m.getTexto());
        StorageReference pictureStorageReference = FirebaseStorage.getInstance().getReference(
                String.format(
                        Locale.getDefault(), "images/%s/profilePic.jpg", m.getEmail().replace("@", "")
                )
        );


        int i = mensagens.size()-1;

        if (position == i){
            if(chat.hasFoto == true) {
                chat.hasFoto = false;
                StorageReference pictureSendedStorageReference = FirebaseStorage.getInstance().getReference(
                        String.format(
                                Locale.getDefault(), "images/%s/sendedPic.jpg", m.getEmail().replace("@", "")
                        )
                );

                pictureSendedStorageReference.getDownloadUrl()
                        .addOnSuccessListener((result) -> {
                            Glide.with(context)
                                    .asBitmap()
                                    .addListener(new RequestListener<Bitmap>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                            resource = chat.picture;
                                            fotoSended.put(m.getEmail(), resource);
                                            holder.sendedImageView.setImageBitmap(resource);
                                            return true;
                                        }
                                    }).load(pictureSendedStorageReference).into(holder.sendedImageView);
                        }).addOnFailureListener((exception) -> {
                    holder.sendedImageView.setImageResource(0);
                });
            }else {
                holder.sendedImageView.setImageResource(0);
            }
        }


        if(fotos.containsKey(m.getEmail())){
            holder.profilePicImageView.setImageBitmap(fotos.get(m.getEmail()));
        }
        else{
            pictureStorageReference.getDownloadUrl()
                    .addOnSuccessListener( (result) -> {
                        Glide.with(context)
                                .asBitmap()
                                .addListener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        fotos.put(m.getEmail(), resource);
                                        holder.profilePicImageView.setImageBitmap(resource);
                                        return true;
                                    }
                                }).load(pictureStorageReference).into(holder.profilePicImageView);
                    }).addOnFailureListener((exception) -> {
                holder.profilePicImageView.setImageResource(R.drawable.ic_person_black_50dp);
            });
        }

    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

}