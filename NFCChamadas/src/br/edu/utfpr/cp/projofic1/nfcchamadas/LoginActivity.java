package br.edu.utfpr.cp.projofic1.nfcchamadas;

import java.sql.SQLException;

import br.edu.utfpr.cp.projofic1.nfcchamadas.database.DatabaseDAO;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private static final int REQUEST_CADASTRAR_USUARIO = 1;
	
	private EditText etRAcademicoOuEmail, etSenha;
	private Button bLogin, bCadastreSe;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Pegando as views
        etRAcademicoOuEmail = (EditText) findViewById(R.id.etRAcademicoOuEmail);
        etSenha = (EditText) findViewById(R.id.etSenhaLogin);
        bLogin = (Button) findViewById(R.id.bLogin);
        bCadastreSe = (Button) findViewById(R.id.bCadastreSe);
        
        //Criando preference para verificar se já está logado 
        SharedPreferences preferences= getSharedPreferences("logado", Context.MODE_PRIVATE);
        
        if(preferences.getBoolean("status", true)){

        
        // Criando os eventos dos bot�es
        bLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				login(etRAcademicoOuEmail.getText().toString().trim(), etSenha.getText().toString());
			}
		});
        bCadastreSe.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(LoginActivity.this, CadastrarUsuarioActivity.class), REQUEST_CADASTRAR_USUARIO);
			}
		});
        }else{
        	startActivityForResult(new Intent(LoginActivity.this, CadastrarUsuarioActivity.class), REQUEST_CADASTRAR_USUARIO);
        	
        }
    }
    
    
    private void login(final String RAcademicoOuEmail, final String senha) {
    	final ProgressDialog progDial = ProgressDialog.show(this, "", "");
    	progDial.setCancelable(false);
    	
    	new AsyncTask<Void, Void, Object>() {
    		
    		@Override
    		protected Object doInBackground(Void... params) {
    			DatabaseDAO dbDAO = null;
    			
    			try {
    				// Fazendo o login do usu�rio, recuperando o ID no banco de dados
    				dbDAO = new DatabaseDAO();
    				Long pessoaId = dbDAO.loginUsuario(RAcademicoOuEmail, senha);
    				return pessoaId;
    				
    			} catch (SQLException e) {
    				// Caso haja algum problema ao fazer a opera��o no banco
    				return e;
    				
    			} finally {
    				// Deve SEMPRE fechar a conex�o com o banco de dados
    				try {
    					if (dbDAO != null)
    						dbDAO.close();
    				} catch (SQLException e) {
    					Log.e("Conex�o com o Banco de Dados com o servidor", "Falha ao fechar a conex�o", e);
    				}
    			}
    		}
    		
    		@Override
    		protected void onPostExecute(Object result) {
    			progDial.cancel();
    			
    			if (result instanceof SQLException) {
    				// Caso houve um problema na opera��o com o Banco de dados
    				Log.e("Conex�o com o Banco de dados no servidor", "Falha ao fazer login", (SQLException) result);
    				Toast.makeText(LoginActivity.this, R.string.falha_ao_fazer_login, Toast.LENGTH_SHORT).show();
    				
    			} else if (result instanceof Long) {
    				// Caso exista um usu�rio com esta senha
    				Long pessoaId = (Long) result;
    				Intent i = new Intent(LoginActivity.this, LogadoActivity.class);
    				i.putExtra(LogadoActivity.EXTRA_PESSOA_ID, pessoaId);
    				startActivity(i);
    				LoginActivity.this.finish();
    			} else {
					// Caso n�o exista um usu�rio com esta senha
					Toast.makeText(LoginActivity.this, R.string.usuario_ou_senha_incorretos, Toast.LENGTH_SHORT).show();
				}
    		}
		}.execute();
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (requestCode == REQUEST_CADASTRAR_USUARIO) {
    		if (resultCode == RESULT_OK) {
    			login(data.getStringExtra(CadastrarUsuarioActivity.RESULT_DATA_RACADEMICO),
    					data.getStringExtra(CadastrarUsuarioActivity.RESULT_DATA_SENHA));
    		}
    	}
    }
}