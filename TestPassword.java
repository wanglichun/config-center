import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = "$2a$10$7JB720yubVSEi/k4sFNr.B2fHlYrm2XZpHLzPo/RbxLy5Hh8JZjiq";
        
        System.out.println("要测试的哈希值: " + hashedPassword);
        System.out.println("=================");
        
        // 测试常见的密码
        String[] testPasswords = {
            "admin123",
            "admin", 
            "123456", 
            "password", 
            "admin@123",
            "root",
            "root123",
            "123",
            "admin888",
            "admin666",
            "password123",
            "12345678",
            "qwerty",
            "admin1",
            "admin12",
            "system"
        };
        
        for (String testPwd : testPasswords) {
            boolean matches = encoder.matches(testPwd, hashedPassword);
            System.out.println("测试密码 '" + testPwd + "': " + (matches ? "✓ 匹配!" : "✗ 不匹配"));
            if (matches) {
                System.out.println("找到正确密码: " + testPwd);
                break;
            }
        }
        
        System.out.println("=================");
        // 生成 admin123 的正确哈希值
        String correctHash = encoder.encode("admin123");
        System.out.println("admin123 的正确BCrypt哈希值: " + correctHash);
        System.out.println("验证新哈希值: " + encoder.matches("admin123", correctHash));
    }
} 