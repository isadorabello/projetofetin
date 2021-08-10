package com.example.farmaapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.farmaapp.R;
import com.example.farmaapp.helper.ConfiguracaoFirebase;
import com.example.farmaapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class AutenticacaoActivity extends AppCompatActivity {

    private Button buttonAcessar;
    private EditText campoEmail, campoSenha;
    private boolean tipo;

    private static FirebaseAuth autenticacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacao);

        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        //autenticacao.signOut();

        buttonAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if(!email.isEmpty()){
                    if(!senha.isEmpty()){
                        usuario = new Usuario();
                        usuario.setEmail(email);
                        usuario.setSenha(senha);
                        validarLogin(usuario);
                    }else{
                        Toast.makeText(AutenticacaoActivity.this, "Informe a senha", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(AutenticacaoActivity.this, "Informe o email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificarUsuarioLogado();
    }

    private void verificarUsuarioLogado(){
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if(usuarioAtual != null){
            recuperarDados();
            if(tipo){
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            }else{
                Toast.makeText(AutenticacaoActivity.this, "Página da Empresa", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(getApplicationContext(), EmpresaActivity.class));
                //finish();
            }
        }
    }

    public void abrirCadastro(View view){
        startActivity(new Intent(AutenticacaoActivity.this, CadastroActivity.class));
    }

    private void inicializarComponentes(){
        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
        buttonAcessar = findViewById(R.id.buttonAcesso);
    }

    public void validarLogin(Usuario usuario){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            recuperarDados();
                            if(tipo){
                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                finish();
                            }else{
                                Toast.makeText(AutenticacaoActivity.this, "Página da Empresa", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(getApplicationContext(), EmpresaActivity.class));
                                //finish();
                            }
                        }else{
                            String excecao;
                            try{
                                throw  task.getException();
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                excecao = "Email e senha não correspondem a um usuário cadastrado!";
                            }catch (FirebaseAuthInvalidUserException e){
                                excecao = "Usuário não está cadastrado.";
                            }catch (Exception e){
                                excecao = "Erro ao cadastrar ususário: " + e .getMessage();
                                e.printStackTrace();
                            }

                            Toast.makeText(AutenticacaoActivity.this, excecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void recuperarDados(){
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(autenticacao.getCurrentUser().getUid());
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario u =snapshot.getValue(Usuario.class);
                String tipoUsuario = u.getTipo();
                if(tipoUsuario.equals("C")){
                    tipo = true;//CLIENTE
                }else{
                    tipo = false;//ESTABELECIMENTO
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}