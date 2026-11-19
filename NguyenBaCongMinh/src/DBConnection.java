import java.sql.*;

/** Kết nối MySQL. Sửa USER / PASS cho đúng với máy của bạn. */
public class DBConnection {
    private static final String URL =
        "jdbc:mysql://localhost:3306/quanlycuahang?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh";
    private static final String USER = "root";
    private static final String PASS = "";   // <-- điền mật khẩu MySQL của bạn (XAMPP mặc định để trống)

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
