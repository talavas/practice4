package shpp.level3.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shpp.level3.util.DBConnection;

import java.sql.*;

public class Store {
    private final Logger logger = LoggerFactory.getLogger(Store.class);

    public static void setConnection(DBConnection connection) {
        Store.connection = connection.getConnection();
    }

    static Connection connection;

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    private String storeType;


    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    private String cityName;

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    private String streetName;

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    private String streetNumber;

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    private int maxQuantity;

    public int getProductTypeIdByName(String name){
        String sqlStm = "SELECT id FROM retail.product_type WHERE name = '" + name + "';";
        try(PreparedStatement typeIdStatement = connection.prepareStatement(sqlStm);
        ResultSet typeIdResultSet = typeIdStatement.executeQuery()){
            if (typeIdResultSet.next()){
                return typeIdResultSet.getInt("id");
            }
        } catch (SQLException e) {
            logger.error("Can't execute sql statement {}", sqlStm);
        }
        logger.debug("Can't find product_type_id by name={}", name);
        return -1;
    }

    public Store findStoreWithMaxInventory(String productType) throws SQLException {
        Store result = null;

        String sqlStm = "SELECT store_id, SUM(quantity) AS max_quantity "+
                "FROM product p " +
                "JOIN "+
                    "(SELECT store_id, product_id, quantity "+
                    "FROM inventory "+
                    "WHERE product_id IN ( "+
                        "SELECT id FROM product WHERE product_type_id = ?) "+
                ") i ON p.id = i.product_id "+
                "GROUP BY store_id " +
                "ORDER BY SUM(quantity) DESC "+
                "LIMIT 1";
        int productTypeId = getProductTypeIdByName(productType);
        logger.debug("product_type_id = {}", productTypeId);
        
        if(productTypeId > 0){
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStm)) {
                preparedStatement.setInt(1, productTypeId);
                try (ResultSet storeResultSet = preparedStatement.executeQuery()) {
                    if (storeResultSet.next()) {
                        result  = getStoreById(storeResultSet.getInt("store_id"));
                        if(result != null){
                            result.setMaxQuantity(storeResultSet.getInt("max_quantity"));
                        }
                    }else{
                        logger.debug("Can't find store with product type={}", productType);
                        return null;
                    }
                }
            }
        }
        return result;
    }

    public Store getStoreById(int id){
        String sqlStm = "SELECT st.name AS store_name, c.name AS city_name, l.street_name, l.street_number " +
                "FROM store s "+
                "JOIN store_type st ON s.store_type_id = st.id "+
                "JOIN location l ON s.location_id = l.id "+
                "JOIN city c ON l.city_id = c.id " +
                "WHERE s.id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStm)){
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if(resultSet.next()){
                    logger.debug("Found store with id={}", id);
                    Store result = new Store();
                    result.setStoreType(resultSet.getString("store_name"));
                    result.setCityName(resultSet.getString("city_name"));
                    result.setStreetName(resultSet.getString("street_name"));
                    result.setStreetNumber(resultSet.getString("street_number"));
                    return result;
                }
            }

        } catch (SQLException e) {
            logger.error("SQL issue: ", e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Магазин {" +
                "назва='" + storeType + '\'' +
                ", місто='" + cityName + '\'' +
                ", локація='" + streetName + '\'' +
                ", номер локації='" + streetNumber + '\'' +
                '}';
    }

}
