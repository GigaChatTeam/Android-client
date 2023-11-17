package com.gct.cl.android;

import static java.lang.String.join;

import androidx.annotation.NonNull;

import com.jsoniter.annotation.JsonIgnore;
import com.jsoniter.output.JsonStream;

public class CommandPackets {
    static class Systems {
        static class Listen {
            static class Add {
                @JsonIgnore
                String[] intention = Commands.SYSTEM_CHANNELS_LISTEN_ADD.intents;

                long client;
                long channel;

                Add (long client, long channel) {
                    this.client = client;
                    this.channel = channel;
                }

                String serialize (@NonNull String hash) {
                    return join("-", intention) + "%" + hash + "%" + JsonStream.serialize(this);
                }
            }

            static class Remove {
                @JsonIgnore
                String[] intention = Commands.SYSTEM_CHANNELS_LISTEN_REMOVE.intents;

                long client;
                long channel;

                Remove (long client, long channel) {
                    this.client = client;
                    this.channel = channel;
                }

                String serialize (@NonNull String hash) {
                    return join("-", intention) + "%" + hash + "%" + JsonStream.serialize(this);
                }
            }
        }
    }

    static class Channels {
        static class Messages {
            static class Post {
                static class New {
                    @JsonIgnore
                    String[] intention = Commands.USER_CHANNELS_MESSAGES_POST_NEW.intents;

                    long author;
                    long channel;
                    @NonNull
                    String text;

                    New (long author, long channel, @NonNull String text) {
                        this.author = author;
                        this.channel = channel;
                        this.text = text;
                    }

                    String serialize (@NonNull String hash) {
                        return join("-", intention) + "%" + hash + "%" + JsonStream.serialize(this);
                    }
                }
            }
        }
    }
}
