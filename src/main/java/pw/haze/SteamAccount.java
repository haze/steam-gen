package pw.haze;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Haze
 * @since 9/24/2015
 */
public class SteamAccount {
    private String id, url, visibility, profileName;
    private long lastLogoff;

    public SteamAccount(String id, String url, String visibility, String profileName, long lastLogoff) {
        this.id = id;
        this.url = url;
        this.visibility = visibility;
        this.profileName = profileName;
        this.lastLogoff = lastLogoff;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrettyLogoffDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy h:mm a", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(lastLogoff));
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public long getLastLogoff() {
        return lastLogoff;
    }

    public void setLastLogoff(long lastLogoff) {
        this.lastLogoff = lastLogoff;
    }

    public String guessEmail() {
        if (isValid())
            return String.format("%s@hotmail.com", profileName);
        else
            return "INVALID_ACCOUNT";
    }

    public boolean isValid() {
        boolean pass = true;
        for (Character c : profileName.toCharArray()) {
            pass &= (Character.isLetterOrDigit(c) || c == '_');
        }
        return pass && profileName.length() > 3;
    }
}
