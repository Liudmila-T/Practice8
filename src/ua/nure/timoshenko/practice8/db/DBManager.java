package ua.nure.timoshenko.practice8.db;

import ua.nure.timoshenko.practice8.db.entity.Team;
import ua.nure.timoshenko.practice8.db.entity.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


public final class DBManager {
    private static final String SQL_INSERT_USER =
            "INSERT INTO users VALUES (DEFAULT, ?)";
    private static final String SQL_FIND_ALL_USERS =
            "SELECT * FROM users";


    private static final String SQL_INSERT_TEAM =
            "INSERT INTO teams VALUES (DEFAULT, ?)";
    private static final String SQL_FIND_ALL_GROUPS =
            "SELECT * FROM teams";

    private static final String SQL_FIND_USER_BY_LOGIN =
            "SELECT * FROM users WHERE login=?";

    private static final String SQL_FIND_TEAM_BY_NAME =
            "SELECT * FROM teams WHERE name=?";

    private static final String SQL_INSERT_USER_TO_TEAM =
            "INSERT INTO users_teams VALUES (?, ?)";

    private static final String SQL_FIND_TEAMS_BY_USER_ID =
            "SELECT id, name FROM teams INNER JOIN users_teams ON " +
                    "id = team_id where user_id = ?";
    private static final String SQL_DELETE_TEAM = "DELETE FROM teams WHERE name=?";
    private static final String SQL_UPDATE_TEAM =
            "UPDATE teams SET name=? WHERE id=?";


    private static final String NAME_FILE_PROPERTIES = "app.properties";
    private static final String CONNECTION_URL = "connection.url";
    public static final int INDEX = 1;
    public static final int INDEX2 = 2;

    private static DBManager instance;
    private Connection connection;

    private DBManager(String url) throws SQLException {
        connection = DriverManager.getConnection(url);
    }

    public static String loadProperties(String nameFile, String nameProperty) throws IOException {
        Properties properties = new Properties();

        properties.load(new FileInputStream(nameFile));

        return properties.getProperty(nameProperty);
    }

    public static synchronized DBManager getInstance()  {
        if (instance == null) {
            try {
                instance = new DBManager(loadProperties(NAME_FILE_PROPERTIES, CONNECTION_URL));
            } catch (SQLException |IOException e) {
                e.printStackTrace();
                throw new IllegalStateException();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }


    public boolean insertUser(User user) throws SQLException {
        boolean res = false;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = connection.prepareStatement(SQL_INSERT_USER,
                    Statement.RETURN_GENERATED_KEYS);
            int k = 1;
            pstmt.setString(k++, user.getLogin());

            if (pstmt.executeUpdate() > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    user.setId(userId);
                    res = true;
                }
            }
        } finally {
            DBUtils.close(rs);
            DBUtils.close(pstmt);
        }
        return res;
    }

    public List<User> findAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(SQL_FIND_ALL_USERS);
            while (rs.next()) {
                users.add(DBUtils.extractUser(rs));
            }
        } finally {
            DBUtils.close(rs);
            DBUtils.close(stmt);
        }
        return users;
    }


    public boolean insertTeam(Team team) throws SQLException {
        boolean res = false;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = connection.prepareStatement(SQL_INSERT_TEAM,
                    Statement.RETURN_GENERATED_KEYS);
            int k = 1;
            pstmt.setString(k++, team.getName());

            if (pstmt.executeUpdate() > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int teamsId = rs.getInt(INDEX);
                    team.setId(teamsId);
                    res = true;
                }
            }
        } finally {
            DBUtils.close(rs);
            DBUtils.close(pstmt);
        }
        return res;
    }

    public List<Team> findAllTeams() throws SQLException {
        List<Team> users = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(SQL_FIND_ALL_GROUPS);
            while (rs.next()) {
                users.add(DBUtils.extractTeam(rs));
            }
        } finally {
            DBUtils.close(rs);
            DBUtils.close(stmt);
        }
        return users;
    }

    public User getUser(String login) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        User user = null;
        try {
            stmt = connection.prepareStatement(SQL_FIND_USER_BY_LOGIN);
            stmt.setString(1, login);
            rs = stmt.executeQuery();
            if (rs.next()) {
                user = new User(login);
                user.setId(rs.getInt("id"));
                user.setLogin(rs.getString("login"));
            }
        } finally {
            DBUtils.close(rs);
            DBUtils.close(stmt);
        }
        return user;
    }

    public Team getTeam(String name) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Team team = null;
        try {
            stmt = connection.prepareStatement(SQL_FIND_TEAM_BY_NAME);
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if (rs.next()) {
                team = new Team(name);
                team.setId(rs.getInt("id"));
                team.setName(rs.getString("name"));
            }
        } finally {
            DBUtils.close(rs);
            DBUtils.close(stmt);
        }
        return team;
    }

    public boolean setTeamsForUser(User user, Team... teams) {
        PreparedStatement stmt = null;
        try {
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(SQL_INSERT_USER_TO_TEAM);
            for (Team team : teams) {
                stmt.setInt(INDEX, user.getId());
                stmt.setInt(INDEX2, team.getId());
                stmt.addBatch();
            }
            int[] usersTeams = stmt.executeBatch();
            for (int i : usersTeams) {
                if (i != 1) {
                    return false;
                }
            }
            connection.commit();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            DBUtils.rollback(connection);
        } finally {
            DBUtils.close(stmt);
        }
        return false;
    }

    public List<Team> getUserTeams(User user) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Team> teams = new ArrayList<>();
        try {
            stmt = connection.prepareStatement(SQL_FIND_TEAMS_BY_USER_ID);
            stmt.setInt(INDEX, user.getId());
            rs = stmt.executeQuery();
            while (rs.next()) {
                Team team = new Team(rs.getString(INDEX2));
                teams.add(team);
                team.setId(rs.getInt(INDEX));
            }
        } catch (SQLException e) {
            System.out.println("Can't get teams:" + e.getMessage());
            return Collections.emptyList();
        } finally {
            DBUtils.close(rs);
            DBUtils.close(stmt);
        }
        return teams;
    }

    public boolean deleteTeam(Team team) throws SQLException {
        boolean res;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = connection.prepareStatement(SQL_DELETE_TEAM);
            pstmt.setString(1, team.getName());

            res = pstmt.executeUpdate() > 0;
        } finally {
            DBUtils.close(rs);
            DBUtils.close(pstmt);
        }
        return res;
    }

    public boolean updateTeam(Team team) throws SQLException {
        boolean res;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            int k = 1;
            pstmt = connection.prepareStatement(SQL_UPDATE_TEAM);
            pstmt.setString(k++, team.getName());
            pstmt.setInt(k++, team.getId());

            res = pstmt.executeUpdate() > 0;
        } finally {
            DBUtils.close(rs);
            DBUtils.close(pstmt);
        }
        return res;
    }
}
