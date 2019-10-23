package com.example.chatcomgps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mensagensRecyclerView;
    private ChatAdapter adapter;
    private List<com.example.chatcomgps.Mensagem> mensagens;
    private FirebaseUser fireUser;
    private CollectionReference collMensagensReference;
    private EditText mensagemEditText;
    private TextView locationTextView;
    private ImageView sendedImageView;
    private ImageView mapsImageView;
    private static final int REQ_CODE_CAMERA = 1001;
    private Context context;
    public static boolean hasFoto = false;
    public static Bitmap picture;
    private View view;
    FloatingActionButton fab;

    private void setupFirebase() {
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        collMensagensReference = FirebaseFirestore.getInstance().collection("mensagens");
        collMensagensReference.addSnapshotListener((result, e) -> {
            mensagens.clear();
            for (DocumentSnapshot doc : result.getDocuments()) {
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

    private double latitude;
    private double longitude;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_PERMISSION_CODE_GPS = 1001;

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

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

    }

     public void abreLocalizacao(View view){
//        Intent intent = new Intent(this, com.example.chatcomgps.GpsActivity.class);
//        startActivity(intent);
         Uri uri = Uri.parse(getString(R.string.uri_mapa, latitude, longitude));
         Intent intent = new Intent(Intent.ACTION_VIEW, uri);
         intent.setPackage("com.google.android.apps.maps");
         startActivity(intent);

     }

    public void enviarMensagem(View view) {

        String texto = mensagemEditText.getText().toString();
        com.example.chatcomgps.Mensagem m = new com.example.chatcomgps.Mensagem(texto, new java.util.Date(), fireUser.getEmail());
        collMensagensReference.add(m);
        mensagemEditText.setText("");
    }

    public void tirarFoto(View view) {
        this.view = view;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //dá pra tirar foto?
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQ_CODE_CAMERA);
        } else
            Toast.makeText(this, getString(R.string.cant_take_pic), Toast.LENGTH_SHORT).show();

    }

    private void uploadPicture(Bitmap picture) {
        String currentUser = fireUser.getEmail();
        StorageReference pictureStorageReference = FirebaseStorage.getInstance().getReference(
                String.format(
                        Locale.getDefault(),
                        "images/%s/sendedPic.jpg",
                        currentUser.replace("@", ""))
        );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        //aqui foi feito o upload
        pictureStorageReference.putBytes(bytes);
        hasFoto = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        sendedImageView = findViewById(R.id.sendedImageView);
        if (requestCode == REQ_CODE_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                picture = (Bitmap) data.getExtras().get("data");
                uploadPicture(picture);
                enviarMensagem(view);
            } else {
                Toast.makeText(this, getString(R.string.no_pic_taken), Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {

        if (view == fab) {
 //           (v) -> {


            locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    String locationText = getString(R.string.lat_long, latitude, longitude);
                    com.example.chatcomgps.Mensagem m = new com.example.chatcomgps.Mensagem(new java.util.Date(), fireUser.getEmail(), locationText);
                    collMensagensReference.add(m);
                    //locationTextView.setText(locationText);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000,
                        10,
                        locationListener
                );
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_CODE_GPS
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE_GPS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            2000,
                            10,
                            locationListener
                    );
                }
            } else {
                Toast.makeText(this, getString(R.string.no_gps_no_app), Toast.LENGTH_SHORT).show();
            }
        }

    }

}



class ChatViewHolder extends RecyclerView.ViewHolder{
    public TextView dataNomeTextView;
    public TextView mensagemTextView;
    public TextView locationTextView;
    public ImageView profilePicImageView;
    public ImageView sendedImageView;
    public ImageView mapsImageView;
    public ChatViewHolder (View raiz){
        super(raiz);
        dataNomeTextView = raiz.findViewById(R.id.dataNomeTextView);
        mensagemTextView = raiz.findViewById(R.id.mensagemTextView);
        locationTextView = raiz.findViewById(R.id.locationTextView);
        profilePicImageView = raiz.findViewById(R.id.profilePicImageView);
        sendedImageView = raiz.findViewById(R.id.sendedImageView);
        mapsImageView = raiz.findViewById(R.id.mapsImageView);
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
        holder.locationTextView.setText(m.getLocation());
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

        if (m.getLocation() != null && m.getLocation() != ""){
            holder.mapsImageView.setImageResource(R.drawable.maps);
            holder.mensagemTextView.setText(null);

        }else{
            holder.mapsImageView.setImageResource(0);
        }

    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

}