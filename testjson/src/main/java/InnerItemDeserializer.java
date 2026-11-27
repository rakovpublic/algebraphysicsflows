import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

public class InnerItemDeserializer extends StdDeserializer<InnerItem> {
    public InnerItemDeserializer(){
        this(null);
    }
    protected InnerItemDeserializer(Class<?> vc) {
        super(vc);
    }

    public InnerItem deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
       // System.out.println(jsonParser.readValueAsTree().toString());
        ObjectMapper mapper= new ObjectMapper();
        JsonElement jelement = new com.google.gson.JsonParser().parse(jsonParser.readValueAsTree().toString());
        System.out.println("element"+jelement.toString());
        JsonObject jobject = jelement.getAsJsonObject();


        try {
            String className=jobject.getAsJsonPrimitive("className").getAsString();
            return new InnerItem( className, (InnerInnerItem) mapper.readValue(jobject.getAsJsonObject("innerItem").toString(),Class.forName(className)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
