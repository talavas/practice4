package shpp.level3.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shpp.level3.dbseed.TableSeeder;
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


    public Store() {
    }

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
        String sqlStm = "SELECT s.id, st.name AS store_type_name, l.street_name, l.street_number, c.name AS city_name, SUM(i.quantity) AS max_quantity " +
                "FROM retail.store s " +
                "INNER JOIN retail.store_type st ON s.store_type_id = st.id " +
                "JOIN retail.location l ON s.location_id = l.id " +
                "JOIN retail.city c ON l.city_id = c.id " +
                "INNER JOIN retail.inventory i ON s.id = i.store_id " +
                "INNER JOIN retail.product p ON i.product_id = p.id " +
                "WHERE p.product_type_id = ? " +
                "GROUP BY s.id, st.name, l.street_name, l.street_number, c.name " +
                "ORDER BY max_quantity DESC " +
                "LIMIT 1";
        
        int productTypeId = getProductTypeIdByName(productType);
        
        if(productTypeId > 0){
            try (PreparedStatement storeStatement = connection.prepareStatement(sqlStm)) {
                storeStatement.setInt(1, productTypeId);
                try (ResultSet storeResultSet = storeStatement.executeQuery()) {
                    if (storeResultSet.next()) {
                        setStoreType(storeResultSet.getString("store_type_name"));
                        setCityName(storeResultSet.getString("city_name"));
                        setStreetName(storeResultSet.getString("street_name"));
                        setStreetNumber(storeResultSet.getString("street_number"));
                        setMaxQuantity(storeResultSet.getInt("max_quantity"));
                        return this;
                    }else{
                        logger.debug("Can't find store with product type={}", productType);
                        return null;
                    }
                }
            }
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
