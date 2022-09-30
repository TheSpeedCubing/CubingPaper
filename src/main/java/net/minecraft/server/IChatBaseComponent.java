package net.minecraft.server;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public interface IChatBaseComponent extends Iterable<IChatBaseComponent> {

    IChatBaseComponent setChatModifier(ChatModifier chatmodifier);

    ChatModifier getChatModifier();

    IChatBaseComponent a(String s);

    IChatBaseComponent addSibling(IChatBaseComponent ichatbasecomponent);

    String getText();

    String c();

    List<IChatBaseComponent> a();

    IChatBaseComponent f();

    public static class ChatSerializer implements JsonDeserializer<IChatBaseComponent>, JsonSerializer<IChatBaseComponent> {

        private static final int CHATCOMPONENT_LIMIT = 128;
        private static final Gson a;

        public ChatSerializer() {
        }

        public IChatBaseComponent a(int extraCount, String lastElement, JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            if (jsonelement.isJsonPrimitive()) {
                return new ChatComponentText(jsonelement.getAsString());
            } else if (!jsonelement.isJsonObject()) {
                if (jsonelement.isJsonArray()) {
                    JsonArray jsonarray = jsonelement.getAsJsonArray();
                    if (jsonarray.size() > CHATCOMPONENT_LIMIT) {
                        throw new JsonParseException("Too many ChatComponent array elements");
                    }
                    IChatBaseComponent ichatbasecomponent = null;
                    Iterator iterator = jsonarray.iterator();

                    while (iterator.hasNext()) {
                        JsonElement jsonelement1 = (JsonElement) iterator.next();
                        if (jsonelement1.isJsonArray()) {
                            throw new JsonParseException("Stacked ChatComponent array");
                        }
                        IChatBaseComponent ichatbasecomponent1 = this.a(jsonelement1, (Type) jsonelement1.getClass(), jsondeserializationcontext);

                        if (ichatbasecomponent == null) {
                            ichatbasecomponent = ichatbasecomponent1;
                        } else {
                            ichatbasecomponent.addSibling(ichatbasecomponent1);
                        }
                    }

                    return ichatbasecomponent;
                } else {
                    throw new JsonParseException("Don\'t know how to turn " + jsonelement.toString() + " into a Component");
                }
            } else {
                JsonObject jsonobject = jsonelement.getAsJsonObject();
                Object object;

                if (jsonobject.has("text")) {
                    object = new ChatComponentText(jsonobject.get("text").getAsString());
                } else if (jsonobject.has("translate")) {
                    String s = jsonobject.get("translate").getAsString();

                    if (jsonobject.has("with")) {
                        JsonArray jsonarray1 = jsonobject.getAsJsonArray("with");
                        if (jsonarray1.size() > CHATCOMPONENT_LIMIT) {
                            throw new JsonParseException("Too many ChatComponent array elements in with");
                        }
                        if (lastElement != null && lastElement.equals("with")) {
                            throw new JsonParseException("Stacked with ChatComponent elements");
                        }
                        Object[] aobject = new Object[jsonarray1.size()];

                        for (int i = 0; i < aobject.length; ++i) {
                            if (jsonarray1.get(i).isJsonArray()) {
                                throw new JsonParseException("Stacked ChatComponent array in with element");
                            }
                            aobject[i] = this.a(extraCount, "with", jsonarray1.get(i), type, jsondeserializationcontext);
                            if (aobject[i] instanceof ChatComponentText) {
                                ChatComponentText chatcomponenttext = (ChatComponentText) aobject[i];

                                if (chatcomponenttext.getChatModifier().g() && chatcomponenttext.a().isEmpty()) {
                                    aobject[i] = chatcomponenttext.g();
                                }
                            }
                        }

                        object = new ChatMessage(s, aobject);
                    } else {
                        object = new ChatMessage(s, new Object[0]);
                    }
                } else if (jsonobject.has("score")) {
                    JsonObject jsonobject1 = jsonobject.getAsJsonObject("score");

                    if (!jsonobject1.has("name") || !jsonobject1.has("objective")) {
                        throw new JsonParseException("A score component needs a least a name and an objective");
                    }

                    object = new ChatComponentScore(ChatDeserializer.h(jsonobject1, "name"), ChatDeserializer.h(jsonobject1, "objective"));
                    if (jsonobject1.has("value")) {
                        ((ChatComponentScore) object).b(ChatDeserializer.h(jsonobject1, "value"));
                    }
                } else {
                    if (!jsonobject.has("selector")) {
                        throw new JsonParseException("Don\'t know how to turn " + jsonelement.toString() + " into a Component");
                    }

                    object = new ChatComponentSelector(ChatDeserializer.h(jsonobject, "selector"));
                }

                if (jsonobject.has("extra")) {
                    JsonArray jsonarray2 = jsonobject.getAsJsonArray("extra");
                    if (jsonarray2.size() > CHATCOMPONENT_LIMIT) {
                        throw new JsonParseException("Too many ChatComponent array elements in extra");
                    }
                    if (extraCount > CHATCOMPONENT_LIMIT) {
                        throw new JsonParseException("Too many stacked extra ChatComponent elements");
                    }
                    if (jsonarray2.size() <= 0) {
                        throw new JsonParseException("Unexpected empty array of components");
                    }

                    for (int j = 0; j < jsonarray2.size(); ++j) {
                        if (jsonarray2.get(j).isJsonArray()) {
                            throw new JsonParseException("Stacked ChatComponent array in extra element");
                        }
                        ((IChatBaseComponent) object).addSibling(this.a(++extraCount, "extra", jsonarray2.get(j), type, jsondeserializationcontext));
                    }
                }

                ((IChatBaseComponent) object).setChatModifier((ChatModifier) jsondeserializationcontext.deserialize(jsonelement, ChatModifier.class));
                return (IChatBaseComponent) object;
            }
        }

        public IChatBaseComponent a(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            return a(0, null, jsonelement, type, jsondeserializationcontext);
        }

        private void a(ChatModifier chatmodifier, JsonObject jsonobject, JsonSerializationContext jsonserializationcontext) {
            JsonElement jsonelement = jsonserializationcontext.serialize(chatmodifier);

            if (jsonelement.isJsonObject()) {
                JsonObject jsonobject1 = (JsonObject) jsonelement;
                Iterator iterator = jsonobject1.entrySet().iterator();

                while (iterator.hasNext()) {
                    Entry entry = (Entry) iterator.next();

                    jsonobject.add((String) entry.getKey(), (JsonElement) entry.getValue());
                }
            }

        }

        public JsonElement a(IChatBaseComponent ichatbasecomponent, Type type, JsonSerializationContext jsonserializationcontext) {
            if (ichatbasecomponent instanceof ChatComponentText && ichatbasecomponent.getChatModifier().g() && ichatbasecomponent.a().isEmpty()) {
                return new JsonPrimitive(((ChatComponentText) ichatbasecomponent).g());
            } else {
                JsonObject jsonobject = new JsonObject();

                if (!ichatbasecomponent.getChatModifier().g()) {
                    this.a(ichatbasecomponent.getChatModifier(), jsonobject, jsonserializationcontext);
                }

                if (!ichatbasecomponent.a().isEmpty()) {
                    JsonArray jsonarray = new JsonArray();
                    Iterator iterator = ichatbasecomponent.a().iterator();

                    while (iterator.hasNext()) {
                        IChatBaseComponent ichatbasecomponent1 = (IChatBaseComponent) iterator.next();

                        jsonarray.add(this.a(ichatbasecomponent1, (Type) ichatbasecomponent1.getClass(), jsonserializationcontext));
                    }

                    jsonobject.add("extra", jsonarray);
                }

                if (ichatbasecomponent instanceof ChatComponentText) {
                    jsonobject.addProperty("text", ((ChatComponentText) ichatbasecomponent).g());
                } else if (ichatbasecomponent instanceof ChatMessage) {
                    ChatMessage chatmessage = (ChatMessage) ichatbasecomponent;

                    jsonobject.addProperty("translate", chatmessage.i());
                    if (chatmessage.j() != null && chatmessage.j().length > 0) {
                        JsonArray jsonarray1 = new JsonArray();
                        Object[] aobject = chatmessage.j();
                        int i = aobject.length;

                        for (int j = 0; j < i; ++j) {
                            Object object = aobject[j];

                            if (object instanceof IChatBaseComponent) {
                                jsonarray1.add(this.a((IChatBaseComponent) object, (Type) object.getClass(), jsonserializationcontext));
                            } else {
                                jsonarray1.add(new JsonPrimitive(String.valueOf(object)));
                            }
                        }

                        jsonobject.add("with", jsonarray1);
                    }
                } else if (ichatbasecomponent instanceof ChatComponentScore) {
                    ChatComponentScore chatcomponentscore = (ChatComponentScore) ichatbasecomponent;
                    JsonObject jsonobject1 = new JsonObject();

                    jsonobject1.addProperty("name", chatcomponentscore.g());
                    jsonobject1.addProperty("objective", chatcomponentscore.h());
                    jsonobject1.addProperty("value", chatcomponentscore.getText());
                    jsonobject.add("score", jsonobject1);
                } else {
                    if (!(ichatbasecomponent instanceof ChatComponentSelector)) {
                        throw new IllegalArgumentException("Don\'t know how to serialize " + ichatbasecomponent + " as a Component");
                    }

                    ChatComponentSelector chatcomponentselector = (ChatComponentSelector) ichatbasecomponent;

                    jsonobject.addProperty("selector", chatcomponentselector.g());
                }

                return jsonobject;
            }
        }

        public static String a(IChatBaseComponent ichatbasecomponent) {
            return IChatBaseComponent.ChatSerializer.a.toJson(ichatbasecomponent);
        }

        public static IChatBaseComponent a(String s) {
            return (IChatBaseComponent) IChatBaseComponent.ChatSerializer.a.fromJson(s, IChatBaseComponent.class);
        }

        @Override
        public JsonElement serialize(IChatBaseComponent object, Type type, JsonSerializationContext jsonserializationcontext) {
            return this.a((IChatBaseComponent) object, type, jsonserializationcontext);
        }

        @Override
        public IChatBaseComponent deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            return this.a(jsonelement, type, jsondeserializationcontext);
        }

        static {
            GsonBuilder gsonbuilder = new GsonBuilder();

            gsonbuilder.registerTypeHierarchyAdapter(IChatBaseComponent.class, new IChatBaseComponent.ChatSerializer());
            gsonbuilder.registerTypeHierarchyAdapter(ChatModifier.class, new ChatModifier.ChatModifierSerializer());
            gsonbuilder.registerTypeAdapterFactory(new ChatTypeAdapterFactory());
            a = gsonbuilder.create();
        }
    }
}