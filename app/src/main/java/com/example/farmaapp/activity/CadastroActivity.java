package com.example.farmaapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.farmaapp.R;
import com.example.farmaapp.helper.ConfiguracaoFirebase;
import com.example.farmaapp.helper.UsuarioFirebase;
import com.example.farmaapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private Switch tipo;
    private LinearLayout linearCliente, linearEstabelecimento;
    private Button buttonCadastrar;
    private EditText campoNome, campoEmail, campoSenha, campoTelefone, campoEndereco, campoNomeEstabelecimento;

    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarComponentes();

        tipo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){//EMPRESA
                    linearEstabelecimento.setVisibility(View.VISIBLE);
                }else{//CLIENTE
                    linearEstabelecimento.setVisibility(View.GONE);
                }
            }
        });

        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();
                String textoTelefone = campoTelefone.getText().toString();
                String textoEndereco = campoEndereco.getText().toString();
                String tipoU = getTipoUsuario();

                if(!textoNome.isEmpty()){
                    if(!textoEmail.isEmpty()){
                        if(!textoSenha.isEmpty()){
                            if(!textoTelefone.isEmpty()){
                                if(!textoEndereco.isEmpty()){
                                    if(tipoU.equals("E")){//Empresa
                                        String textNomeE = campoNomeEstabelecimento.getText().toString();
                                        if(!textNomeE.isEmpty()){
                                            usuario = new Usuario();
                                            usuario.setNome(textoNome);
                                            usuario.setEmail(textoEmail);
                                            usuario.setSenha(textoSenha);
                                            usuario.setTelefonePessoal(textoTelefone);
                                            usuario.setEndereco(textoEndereco);
                                            usuario.setNomeEmpresa(textNomeE);
                                            usuario.setTipo(tipoU);
                                            cadastrar(usuario);
                                        }else{
                                            Toast.makeText(CadastroActivity.this, "Informe o nome do estabelecimento!", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{//Cliente
                                        usuario = new Usuario();
                                        usuario.setNome(textoNome);
                                        usuario.setEmail(textoEmail);
                                        usuario.setSenha(textoSenha);
                                        usuario.setTelefonePessoal(textoTelefone);
                                        usuario.setEndereco(textoEndereco);
                                        usuario.setTipo(tipoU);
                                        cadastrar(usuario);
                                    }
                                }else{
                                    Toast.makeText(CadastroActivity.this, "Informe um endereço!", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(CadastroActivity.this, "Informe um telefone!", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(CadastroActivity.this, "Informe uma senha!", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(CadastroActivity.this, "Informe o email!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CadastroActivity.this, "Informe o nome!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void inicializarComponentes(){
        tipo = findViewById(R.id.switchTipo);
        linearCliente = findViewById(R.id.linearCliente);
        linearEstabelecimento = findViewById(R.id.linearEstabelecimento);
        buttonCadastrar = findViewById(R.id.buttonCadastro);
        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        campoTelefone = findViewById(R.id.editCadastroTelefone);
        campoEndereco = findViewById(R.id.editCadastroEndereco);
        campoNomeEstabelecimento = findViewById(R.id.editCadastroEstabelecimento);
    }

    private String getTipoUsuario(){
        //Se estiver configurado, retorna o tipo E, de empresa/estabelecimento, senão, retorna C, de cliente.
        return tipo.isChecked() ? "E" : "C";
    }

    public void cadastrar(Usuario u){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(u.getEmail(), u.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    try{
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId(idUsuario);
                        usuario.salvar();

                        //salvar dados no profile do firebse
                        UsuarioFirebase.atulizarNomeUsuario(usuario.getNome());

                        String t = usuario.getTipo();
                        if(t.equals("C")){
                            Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                            finish();
                        }else{
                            Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                            //startActivity(new Intent(getApplicationContext(),EmpresaActivity.class));
                            //finish();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    String excecao ="";
                    try{
                        throw  task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Digite um email válido";
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao = "Esse email ja foi cadastrado";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar ususário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}