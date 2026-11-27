import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String [] args){
        InnerInnerItem it = new InnerInnerItem();
        String json= "{\"innerItem\" : {\"className\":\""+it.getClass().getCanonicalName()+"\",\"innerItem\":{\"string\":\"someValue\",\"aDouble\":12}}}";
        System.out.println(json);
        ObjectMapper mapper= new ObjectMapper();
        try {
            Item item = mapper.readValue(json,Item.class);
            System.out.println(item.toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
