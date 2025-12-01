package com.vms.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vms.backend.entity.Host;
import com.vms.backend.repository.HostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Autowired
	private HostRepository hostRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Bean
	public CommandLineRunner dataLoader() {
		return args -> {
			// Find existing admin or create new one
			Host admin = hostRepository.findByUsername("admin").orElse(new Host());

			// Always set these values to ensure they are correct
			admin.setName("Super Admin");
			admin.setEmail("akashnilkund19@gmail.com");
			admin.setMobile("0000000000");
			admin.setDepartment("IT");
			admin.setUsername("admin");
			admin.setPassword(passwordEncoder.encode("password")); // Force reset password
			admin.setRole("ADMIN");

			hostRepository.save(admin);
			System.out.println("Admin user updated: username=admin, password=password");
		};
	}

}