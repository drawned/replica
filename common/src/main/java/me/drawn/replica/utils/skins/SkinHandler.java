package me.drawn.replica.utils.skins;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.drawn.replica.Replica;
import me.drawn.replica.npc.NPCData;
import me.drawn.replica.npc.custom.PlayerNPC;
import me.drawn.replica.utils.Utils;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class SkinHandler {

    public static boolean isValidUrl(String skinUrl) {
        try {
            URL url = new URL(skinUrl);

            String protocol = url.getProtocol();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                return false;
            }

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    // MineSkin
    public static void getAndApplyFromURL(@Nullable CommandSender requester, String skinURL, PlayerNPC npc) {
        Replica.log("Requesting new skin to mineskin.org");

        if(!isValidUrl(skinURL)) {
            if(requester != null)
                Utils.warningMessage(requester, "You must provide a valid image URL.");
            return;
        }

        Replica.getScheduler().runTaskAsynchronously(() -> {
            try {
                URL url = new URL("https://api.mineskin.org/generate/url");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                String body = "{\"url\": \"" + skinURL + "\"}";
                conn.getOutputStream().write(body.getBytes());

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                String uuid = json.get("uuid").getAsString();

                JsonObject texture = json.getAsJsonObject("data").getAsJsonObject("texture");
                String value = texture.get("value").getAsString();
                String signature = texture.get("signature").getAsString();

                npc.applySkin(new NPCData.SkinTexture(signature, value), fromRawUUID(uuid));

                if(requester != null)
                    Utils.normalMessage(requester,"Skin applied successfully!");
            } catch (Exception ex) {
                if(requester != null)
                    Utils.warningMessage(requester,"An error occurred trying to apply this skin to the NPC. Error: "+ex.getMessage());
                ex.fillInStackTrace();
            }
        });
    }

    public static UUID fromRawUUID(String raw) {
        if (raw.length() != 32) throw new IllegalArgumentException("Invalid UUID");
        String formatted = raw.substring(0, 8) + "-" +
                raw.substring(8, 12) + "-" +
                raw.substring(12, 16) + "-" +
                raw.substring(16, 20) + "-" +
                raw.substring(20, 32);
        return UUID.fromString(formatted);
    }

    // Mojang
    public static void getAndApplyFromName(@Nullable CommandSender requester, String username, PlayerNPC npc) {
        Replica.log("Requesting new skin from Mojang");
        Replica.getScheduler().runTaskAsynchronously(() -> {
            try {
                // 1- Search UUID
                String uuidUrl = "https://api.mojang.com/users/profiles/minecraft/" + username;
                String uuidResponse = httpGet(uuidUrl);

                if (uuidResponse == null || uuidResponse.isEmpty()) {
                    throw new IllegalArgumentException("Username not found: " + username);
                }

                JsonObject uuidJson = JsonParser.parseString(uuidResponse).getAsJsonObject();
                String uuid = uuidJson.get("id").getAsString();

                // 2- Get raw texture and signature
                String sessionUrl = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false";
                String sessionResponse = httpGet(sessionUrl);
                JsonObject sessionJson = JsonParser.parseString(sessionResponse).getAsJsonObject();

                JsonObject properties = sessionJson.getAsJsonArray("properties").get(0).getAsJsonObject();
                String value = properties.get("value").getAsString();
                String signature = properties.get("signature").getAsString();

                npc.applySkin(new NPCData.SkinTexture(signature, value), fromRawUUID(uuid));

                if(requester != null)
                    Utils.normalMessage(requester,"Skin applied successfully!");
            } catch (Exception ex) {
                if(requester != null)
                    Utils.warningMessage(requester,"An error occurred trying to apply this skin to the NPC. Error: "+ex.getMessage());
                ex.fillInStackTrace();
            }
        });
    }

    private static String httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
