package com.gct.cl.android;


public class ResponsePackets {
    static class AC {
        static class Authorization {
            static class Done {
                String status;

                AuthData data;
            }
            static class ClientError {
                String status;
                String reason;
                String description;
            }
            static class NotFound {
                String status;
                String reason;
                String description;
            }
        }
        static class AuthData {
            long id;
            String token;
        }
    }
}
