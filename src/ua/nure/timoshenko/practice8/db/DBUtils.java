package ua.nure.timoshenko.practice8.db;

import ua.nure.timoshenko.practice8.db.entity.Team;
import ua.nure.timoshenko.practice8.db.entity.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DBUtils {

    private DBUtils() {}

    public static void close(AutoCloseable ac) {
        if (ac != null) {
            try {
                ac.close();
            } catch (Exception e) {
                // write to log
                e.printStackTrace();
            }
        }
    }

    public static User extractUser(ResultSet rs) throws SQLException {
        User user = new User(rs.getString(Fields.USER_LOGIN));
        user.setId(rs.getInt(Fields.USER_ID));
        return user;
    }

    public static Team extractTeam(ResultSet rs) throws SQLException{
        Team team = new Team(rs.getString(Fields.GROUP_NAME));
        team.setId(rs.getInt(Fields.GROUP_ID));
        return team;
    }

    public static void rollback(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
