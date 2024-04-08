package com.example.BlockchainServer.message;

import com.google.gson.Gson;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

public class MessageDecoder implements Decoder.Text<Object> {

    Gson gson = new Gson();

    @Override
    public Object decode(String s) throws DecodeException {
        return gson.fromJson(s, Object.class);
    }

    @Override
    public boolean willDecode(String s) {
        return (s!=null && !s.isEmpty());
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        Text.super.init(endpointConfig);
    }

    @Override
    public void destroy() {
        Text.super.destroy();
    }
}
