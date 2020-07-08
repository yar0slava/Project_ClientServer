package client_server.proccessing;

import client_server.dao.DaoGroup;
import client_server.dao.DaoProduct;
import client_server.dao.UserDao;
import client_server.domain.*;
import client_server.domain.packet.Message;
import client_server.domain.packet.Packet;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Processor{
    public static byte[] process(byte[] packetFromUser) {//processes packet from USER, gets encoded packet
        Packet packet = new Packet(packetFromUser);//decoding packet from USER

        Message.cTypes [] val = Message.cTypes.values();
        int command = packet.getBMsq().getcType();
        Message.cTypes command_type = val[command];

        String message = new String(packet.getBMsq().getMessage(), StandardCharsets.UTF_8);

        JSONObject information;
        int success;

        JsonResponse reply = new JsonResponse();

        DaoProduct daoProduct;
        DaoGroup daoGroup;
        UserDao daoUser;

        switch(command_type){
            case ADD_USER:
                information = new JSONObject(message);
                User userToAdd = new User( information.getString("login"), DigestUtils.md5Hex(information.getString("password"))
                        ,information.getString("role"));
                daoUser = new UserDao("file.db");
                success = daoUser.insertUser(userToAdd);
                if(success == -1){
                    reply.putField("This user already exists!");
                }
                else{
                    reply.putField("User successfully added!");
                }
                break;
            case LOGIN:
                information = new JSONObject(message);
                UserCredentials userCred = new UserCredentials(information.getString("login"), information.getString("password"));
                daoUser = new UserDao("file.db");
                User user = daoUser.getByLogin(userCred.getLogin());
                if(user != null){
                    if(userCred.getPassword().equals(user.getPassword())){
                        reply.putObject("{\"role\":\""+user.getRole()+"\"}");
                    }
                    else{
                        reply.putField("Wrong login or password!");
                    }
                }else{
                    reply.putField("Wrong login or password!");
                }
                break;
            case INSERT_PRODUCT:
                information = new JSONObject(message);
                 Product product = new Product(information.getString("name"),
                        information.getDouble("price"),information.getDouble("amount"),information.getString("description"),
                        information.getString("manufacturer"),information.getInt("group_id"));
                daoProduct = new DaoProduct("file.db");
                success = daoProduct.insertProduct(product);
                if(success == -1){
                    reply.putField("Failed to add product!");
                }
                else{
                    reply.putField("Product successfully added!");
                }
                break;

            case UPDATE_PRODUCT:
                information = new JSONObject(message);
                Product product2 = new Product( information.getInt("id"),information.getString("name"),
                        information.getDouble("price"),information.getDouble("amount"),information.getString("description"),
                        information.getString("manufacturer"),information.getInt("group_id"));
                daoProduct = new DaoProduct("file.db");
                success = daoProduct.updateProduct(product2);
                if(success == -1){
                    reply.putField("Failed to update product!");
                }
                else{
                    reply.putField( "Product successfully updated!");
                }
                break;

            case DELETE_PRODUCT:
                int id3 = Integer.parseInt(message);
                daoProduct = new DaoProduct("file.db");
                success = daoProduct.deleteProduct(id3);
                if(success == -1){
                    reply.putField("Failed to delete product!");
                }
                else{
                    reply.putField("Product with ID " + success + " successfully deleted!");
                }
                break;

            case GET_PRODUCT:
                int id4 = Integer.parseInt(message);
                daoProduct = new DaoProduct("file.db");
                Product product4 = daoProduct.getProduct(id4);
                if(product4 == null){
                    reply.putField("Invalid product ID!");
                }
                else{
                    reply.putObject(product4.toJSON().toString());
                }
                break;

            case GET_LIST_PRODUCTS:
                information = new JSONObject(message);
                int page = information.getInt("page");
                int size = information.getInt("size");
                JSONObject filtr = information.getJSONObject("productFilter");
                ProductFilter filter = new ProductFilter();
                if(!filtr.isNull("ids")){
                    JSONArray array = filtr.getJSONArray("ids");
                    List<Integer> arrayList = new ArrayList<>();
                    for(int i = 0; i < array.length(); i++){
                        arrayList.add((Integer)(array.get(i)));
                    }
                    filter.setIds(arrayList);
                }
                if(!filtr.isNull("group_id")){
                    filter.setGroup_id(filtr.getInt("group_id"));
                }
                if(!filtr.isNull("manufacturer")){
                    filter.setManufacturer(filtr.getString("manufacturer"));
                }
                if(!filtr.isNull("toPrice")){
                    filter.setToPrice(filtr.getDouble("toPrice"));
                }
                if(!filtr.isNull("fromPrice")){
                    filter.setFromPrice(filtr.getDouble("fromPrice"));
                }
                if(!filtr.isNull("query")){
                    filter.setQuery("query");
                }
                daoProduct = new DaoProduct("file.db");
                List<Product> products = daoProduct.getList(page, size, filter);
                if(products == null){
                    reply.putField("Invalid filters!");
                }
                else{
                    reply.putObject(Product.toJSONObject(products).toString());
                }
                break;

            case GET_PRODUCTS_STATISTICS:
                daoProduct = new DaoProduct("file.db");
                List<ProductStatistics> productStatistics = daoProduct.getStatisticsList(Integer.parseInt(message));

                if(productStatistics == null){
                    reply.putField("Can't show statistics");
                }
                else{
                    reply.putObject(ProductStatistics.toJSONObject(productStatistics).toString());
                }
                break;

            case DELETE_ALL_IN_GROUP:
                int id6 = Integer.parseInt(message);
                daoProduct = new DaoProduct("file.db");
                success = daoProduct.deleteAllInGroup(id6);
                if(success == 1){
                    reply.putField("Products in group " + id6 + " were deleted");
                }
                else{
                    reply.putField("Failed to delete group with products!");
                }
                break;

            case INSERT_GROUP:
                information = new JSONObject(message);
                Group group = new Group( information.getInt("id"),information.getString("name")
                        ,information.getString("description"));
                daoGroup = new DaoGroup("file.db");
                success = daoGroup.insertGroup(group);
                if(success == -1){
                    reply.putField("ID and name should be unique!");
                }
                else{
                    reply.putField("Group successfully added!");
                }
                break;
            case DELETE_GROUP:
                int group_id = Integer.parseInt(message);
                daoGroup = new DaoGroup("file.db");
                success = daoGroup.deleteGroup(group_id);
                if(success == -1){
                    reply.putField("Failed to delete group!");
                }
                else{
                    reply.putField("Group with ID "+success + " successfully deleted!");
                }
                break;
            case UPDATE_GROUP:
                information = new JSONObject(message);
                Group group1 = new Group( information.getInt("id"),information.getString("name")
                        ,information.getString("description"));
                daoGroup = new DaoGroup("file.db");
//                Group getByName = daoGroup.getGroupByName(group1.getName());
                success = daoGroup.updateGroup(group1);
                if(success == -1){
                    reply.putField("Invalid name of group!");
                }
                else{
                    reply.putField("Group successfully updated!");
                }
                break;
            case GET_GROUP:
                int group_id1 = Integer.parseInt(message);
                daoGroup = new DaoGroup("file.db");
                Group resGroup = daoGroup.getGroup(group_id1);
                if(resGroup == null){
                    reply.putField("No such group!");
                }
                else{
                    reply.putObject(resGroup.toJSON().toString());
                }
                break;
            case GET_LIST_GROUPS:
                daoGroup = new DaoGroup("file.db");
                List<Group> groups = daoGroup.getAll();
                if(groups == null){
                    reply.putField("Failed to get groups!");
                }
                else{
                    reply.putObject(Group.toJSONObject(groups).toString());
                }
                break;
            default:
                reply.putField("INVALID COMMAND");
        }

        System.out.println("Message from client: " + new String(packet.getBMsq().getMessage(), StandardCharsets.UTF_8)
                + "\t\t\t and with packet ID: " + packet.getbPktId());

        Message answerMessage = new Message(packet.getBMsq().getcType(), packet.getBMsq().getbUserId(), reply.toString().getBytes(StandardCharsets.UTF_8));
        Packet answerPacket = new Packet(packet.getSrcId(), packet.getbPktId(), answerMessage);

        return answerPacket.toPacket(); //returns encoded response for USER
    }
}

