import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        
        // 生成新的哈希值
        String hash = encoder.encode(password);
        System.out.println("密码: " + password);
        System.out.println("BCrypt哈希值: " + hash);
        System.out.println("验证结果: " + encoder.matches(password, hash));
        
        // 生成SQL更新语句
        System.out.println("\n数据库更新SQL:");
        System.out.println("UPDATE sys_user SET password = '" + hash + "' WHERE username = 'admin';");
    }
} 