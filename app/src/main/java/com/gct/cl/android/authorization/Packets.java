package com.gct.cl.android.authorization;

import androidx.annotation.NonNull;

import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;

public class Packets {
    static class Authorizations {
        static class Done { // 200
            static class AuthData {
                long id;
                String username;
                String token;
            }

            String status;
            AuthData data;
        }

        static class ClientError { // 406
            String status;
            String reason;
            String description;
        }

        static class NotFound { // 404
            String status;
            String reason;
            String description;
        }
    }

    static class Registration {
        static class Done { // 200
            static class RegData {
                long id;
                String username;
                String token;
            }

            String status;
            RegData data;
        }

        static class NotValid { // 400
            String status;
            String reason;
            String target;

            @JsonCreator
            NotValid (
                    @JsonProperty("status") String status,
                    @JsonProperty("reason") String reason,
                    @NonNull @JsonProperty("description") String description) {
                this.status = status;
                this.reason = reason;
                switch (description) {
                    case "BadName":
                        this.target = "name";
                        break;
                    case "BadPassword":
                        this.target = "password";
                        break;
                    case "NotValidContact":
                        this.target = "contact";
                        break;
                    default:
                        throw new NullPointerException();
                }
            }
        }

        static class ClientError { // 406
            String refused;
            String reason;
            String description;
        }

        static class AlreadyRegistered { // 409

            String status;
            String reason;
            String target;

            @JsonCreator
            AlreadyRegistered (
                    @JsonProperty("status") String status,
                    @JsonProperty("reason") String reason,
                    @NonNull @JsonProperty("description") String description) {
                this.status = status;
                this.reason = reason;
                switch (description) {
                    case "UsernameAlreadyRegistered":
                        this.target = "username";
                        break;
                    case "ContactAlreadyRegistered":
                        this.target = "contact";
                        break;
                    default:
                        throw new NullPointerException();
                }
            }
        }
    }
}
