package ma.projet.patternai.requests;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String motdepasse;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotdepasse() {
        return motdepasse;
    }

    public void setMotdepasse(String motdepasse) {
        this.motdepasse = motdepasse;
    }
}