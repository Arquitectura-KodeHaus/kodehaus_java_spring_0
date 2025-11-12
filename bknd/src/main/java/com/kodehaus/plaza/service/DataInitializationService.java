package com.kodehaus.plaza.service;

import com.kodehaus.plaza.entity.*;
import com.kodehaus.plaza.repository.*;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Data initialization service for creating seed data
 * NOTE: Disabled because data already exists in Cloud SQL
 */
// @Component // Disabled - data already exists in production DB
public class DataInitializationService implements CommandLineRunner {
    
    private final PlazaRepository plazaRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final BulletinRepository bulletinRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DataInitializationService(PlazaRepository plazaRepository, UserRepository userRepository,
                                   RoleRepository roleRepository, PermissionRepository permissionRepository,
                                   BulletinRepository bulletinRepository, ProductRepository productRepository,
                                   PasswordEncoder passwordEncoder) {
        this.plazaRepository = plazaRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.bulletinRepository = bulletinRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Starting data initialization...");
        
        // Create permissions first
        createPermissions();
        
        // Create roles
        createRoles();
        
        // Create plaza
        createPlaza();
        
        // Create users
        createUsers();
        
        // Create products
        createProducts();
        
        // Create sample bulletins
        createBulletins();
        
        System.out.println("Data initialization completed successfully!");
    }
    
    private void createPermissions() {
        if (permissionRepository.count() == 0) {
            System.out.println("Creating permissions...");
            
            // User management permissions
            createPermission("USERS_CREATE", "Create users", "USERS", "CREATE");
            createPermission("USERS_READ", "Read users", "USERS", "READ");
            createPermission("USERS_UPDATE", "Update users", "USERS", "UPDATE");
            createPermission("USERS_DELETE", "Delete users", "USERS", "DELETE");
            
            // Role management permissions
            createPermission("ROLES_CREATE", "Create roles", "ROLES", "CREATE");
            createPermission("ROLES_READ", "Read roles", "ROLES", "READ");
            createPermission("ROLES_UPDATE", "Update roles", "ROLES", "UPDATE");
            createPermission("ROLES_DELETE", "Delete roles", "ROLES", "DELETE");
            
            // Bulletin permissions
            createPermission("BULLETINS_CREATE", "Create bulletins", "BULLETINS", "CREATE");
            createPermission("BULLETINS_READ", "Read bulletins", "BULLETINS", "READ");
            createPermission("BULLETINS_UPDATE", "Update bulletins", "BULLETINS", "UPDATE");
            createPermission("BULLETINS_DELETE", "Delete bulletins", "BULLETINS", "DELETE");
            
            // Plaza management permissions
            createPermission("PLAZAS_CREATE", "Create plazas", "PLAZAS", "CREATE");
            createPermission("PLAZAS_READ", "Read plazas", "PLAZAS", "READ");
            createPermission("PLAZAS_UPDATE", "Update plazas", "PLAZAS", "UPDATE");
            createPermission("PLAZAS_DELETE", "Delete plazas", "PLAZAS", "DELETE");
            
            // Security permissions
            createPermission("SECURITY_ACCESS", "Security access", "SECURITY", "ACCESS");
            createPermission("PARKING_ACCESS", "Parking access", "PARKING", "ACCESS");
        }
    }
    
    private void createPermission(String name, String description, String resource, String action) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(description);
        permission.setResource(resource);
        permission.setAction(action);
        permissionRepository.save(permission);
    }
    
    private void createRoles() {
        if (roleRepository.count() == 0) {
            System.out.println("Creating roles...");
            
            // Manager role with all permissions
            Role managerRole = new Role();
            managerRole.setName("MANAGER");
            managerRole.setDescription("Plaza Manager with full access");
            Set<Permission> managerPermissions = new HashSet<>(permissionRepository.findByIsActiveTrue());
            managerRole.setPermissions(managerPermissions);
            roleRepository.save(managerRole);
            
            // Employee Security role
            Role securityRole = new Role();
            securityRole.setName("EMPLOYEE_SECURITY");
            securityRole.setDescription("Security personnel");
            securityRole.setPermissions(Set.of(
                permissionRepository.findByName("BULLETINS_READ").orElse(null),
                permissionRepository.findByName("BULLETINS_CREATE").orElse(null),
                permissionRepository.findByName("SECURITY_ACCESS").orElse(null)
            ));
            roleRepository.save(securityRole);
            
            // Employee Parking role
            Role parkingRole = new Role();
            parkingRole.setName("EMPLOYEE_PARKING");
            parkingRole.setDescription("Parking personnel");
            parkingRole.setPermissions(Set.of(
                permissionRepository.findByName("BULLETINS_READ").orElse(null),
                permissionRepository.findByName("BULLETINS_CREATE").orElse(null),
                permissionRepository.findByName("PARKING_ACCESS").orElse(null)
            ));
            roleRepository.save(parkingRole);
            
            // General Employee role
            Role generalRole = new Role();
            generalRole.setName("EMPLOYEE_GENERAL");
            generalRole.setDescription("General employee");
            generalRole.setPermissions(Set.of(
                permissionRepository.findByName("BULLETINS_READ").orElse(null),
                permissionRepository.findByName("BULLETINS_CREATE").orElse(null)
            ));
            roleRepository.save(generalRole);
        }
    }
    
    private void createPlaza() {
        if (plazaRepository.count() == 0) {
            System.out.println("Creating plaza...");
            
            Plaza plaza = new Plaza();
            plaza.setName("Centro Comercial Plaza Central");
            plaza.setDescription("Modern shopping center in the heart of the city");
            plaza.setAddress("Calle Principal 123, Ciudad Central");
            plaza.setPhoneNumber("+1-555-0123");
            plaza.setEmail("info@plazacentral.com");
            plaza.setOpeningHours("09:00");
            plaza.setClosingHours("22:00");
            plazaRepository.save(plaza);
        }
    }
    
    private void createUsers() {
        if (userRepository.count() == 0) {
            System.out.println("Creating users...");
            
            Plaza plaza = plazaRepository.findByName("Centro Comercial Plaza Central").orElse(null);
            Role managerRole = roleRepository.findByName("MANAGER").orElse(null);
            Role securityRole = roleRepository.findByName("EMPLOYEE_SECURITY").orElse(null);
            Role parkingRole = roleRepository.findByName("EMPLOYEE_PARKING").orElse(null);
            Role generalRole = roleRepository.findByName("EMPLOYEE_GENERAL").orElse(null);
            
            // Create manager
            User manager = new User();
            manager.setUsername("manager1");
            manager.setEmail("manager@plazacentral.com");
            manager.setPassword(passwordEncoder.encode("password123"));
            manager.setFirstName("John");
            manager.setLastName("Doe");
            manager.setPhoneNumber("+1-555-0001");
            manager.setPlaza(plaza);
            manager.setRoles(Set.of(managerRole));
            userRepository.save(manager);
            
            // Create security employee
            User security = new User();
            security.setUsername("security1");
            security.setEmail("security@plazacentral.com");
            security.setPassword(passwordEncoder.encode("password123"));
            security.setFirstName("Jane");
            security.setLastName("Smith");
            security.setPhoneNumber("+1-555-0002");
            security.setPlaza(plaza);
            security.setRoles(Set.of(securityRole));
            userRepository.save(security);
            
            // Create parking employee
            User parking = new User();
            parking.setUsername("parking1");
            parking.setEmail("parking@plazacentral.com");
            parking.setPassword(passwordEncoder.encode("password123"));
            parking.setFirstName("Mike");
            parking.setLastName("Johnson");
            parking.setPhoneNumber("+1-555-0003");
            parking.setPlaza(plaza);
            parking.setRoles(Set.of(parkingRole));
            userRepository.save(parking);
            
            // Create general employee
            User general = new User();
            general.setUsername("employee1");
            general.setEmail("employee@plazacentral.com");
            general.setPassword(passwordEncoder.encode("password123"));
            general.setFirstName("Sarah");
            general.setLastName("Wilson");
            general.setPhoneNumber("+1-555-0004");
            general.setPlaza(plaza);
            general.setRoles(Set.of(generalRole));
            userRepository.save(general);
        }
    }
    
    private void createProducts() {
        if (productRepository.count() == 0) {
            System.out.println("Creating products...");
            
            Plaza plaza = plazaRepository.findByName("Centro Comercial Plaza Central").orElse(null);
            
            if (plaza != null) {
                // Verduras
                createProduct("Tomate", "Tomate rojo fresco", "Verduras", "kg", new BigDecimal("2000.00"), plaza);
                createProduct("Cebolla", "Cebolla blanca", "Verduras", "kg", new BigDecimal("1500.00"), plaza);
                createProduct("Zanahoria", "Zanahoria fresca", "Verduras", "kg", new BigDecimal("1800.00"), plaza);
                createProduct("Lechuga", "Lechuga fresca", "Verduras", "unidad", new BigDecimal("2500.00"), plaza);
                createProduct("Pimentón", "Pimentón rojo", "Verduras", "kg", new BigDecimal("3000.00"), plaza);
                
                // Tubérculos
                createProduct("Papa", "Papa criolla", "Tubérculos", "kg", new BigDecimal("1000.00"), plaza);
                createProduct("Yuca", "Yuca fresca", "Tubérculos", "kg", new BigDecimal("1200.00"), plaza);
                createProduct("Ñame", "Ñame fresco", "Tubérculos", "kg", new BigDecimal("1500.00"), plaza);
                createProduct("Plátano", "Plátano verde", "Tubérculos", "kg", new BigDecimal("2000.00"), plaza);
                
                // Frutas
                createProduct("Banano", "Banano maduro", "Frutas", "kg", new BigDecimal("3000.00"), plaza);
                createProduct("Manzana", "Manzana roja", "Frutas", "kg", new BigDecimal("4000.00"), plaza);
                createProduct("Naranja", "Naranja dulce", "Frutas", "kg", new BigDecimal("3500.00"), plaza);
                createProduct("Uva", "Uva fresca", "Frutas", "kg", new BigDecimal("6000.00"), plaza);
                createProduct("Mango", "Mango maduro", "Frutas", "kg", new BigDecimal("2500.00"), plaza);
                
                // Cárnicos
                createProduct("Pollo", "Pechuga de pollo", "Cárnicos", "kg", new BigDecimal("12000.00"), plaza);
                createProduct("Res", "Carne de res", "Cárnicos", "kg", new BigDecimal("18000.00"), plaza);
                createProduct("Cerdo", "Carne de cerdo", "Cárnicos", "kg", new BigDecimal("15000.00"), plaza);
                createProduct("Pescado", "Pescado fresco", "Cárnicos", "kg", new BigDecimal("20000.00"), plaza);
                
                // Lácteos
                createProduct("Leche", "Leche fresca", "Lácteos", "litro", new BigDecimal("4000.00"), plaza);
                createProduct("Queso", "Queso fresco", "Lácteos", "kg", new BigDecimal("8000.00"), plaza);
                createProduct("Yogurt", "Yogurt natural", "Lácteos", "unidad", new BigDecimal("2000.00"), plaza);
                createProduct("Mantequilla", "Mantequilla", "Lácteos", "unidad", new BigDecimal("3000.00"), plaza);
            }
        }
    }
    
    private void createProduct(String name, String description, String category, String unit, BigDecimal price, Plaza plaza) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setUnit(unit);
        product.setPrice(price);
        product.setIsAvailable(true);
        product.setPlaza(plaza);
        productRepository.save(product);
    }
    
    private void createBulletins() {
        if (bulletinRepository.count() == 0) {
            System.out.println("Creating sample bulletins...");
            
            Plaza plaza = plazaRepository.findByName("Centro Comercial Plaza Central").orElse(null);
            User manager = userRepository.findByUsername("manager1").orElse(null);
            
            if (plaza != null && manager != null) {
                // Create today's bulletin
                Bulletin todayBulletin = new Bulletin();
                todayBulletin.setTitle("Daily Market Prices - " + LocalDate.now());
                todayBulletin.setContent("Fresh produce prices for today:\n" +
                    "• Potatoes: $1000/kg\n" +
                    "• Tomatoes: $2000/kg\n" +
                    "• Onions: $1500/kg\n" +
                    "• Carrots: $1800/kg\n" +
                    "• Lettuce: $2500/kg\n\n" +
                    "Special offers:\n" +
                    "• Buy 2 get 1 free on selected fruits\n" +
                    "• 20% discount on organic vegetables");
                todayBulletin.setPublicationDate(LocalDate.now());
                todayBulletin.setPlaza(plaza);
                todayBulletin.setCreatedBy(manager);
                bulletinRepository.save(todayBulletin);
                
                // Create yesterday's bulletin
                Bulletin yesterdayBulletin = new Bulletin();
                yesterdayBulletin.setTitle("Daily Market Prices - " + LocalDate.now().minusDays(1));
                yesterdayBulletin.setContent("Yesterday's market update:\n" +
                    "• Potatoes: $950/kg\n" +
                    "• Tomatoes: $2100/kg\n" +
                    "• Onions: $1400/kg\n" +
                    "• Carrots: $1700/kg\n" +
                    "• Lettuce: $2400/kg");
                yesterdayBulletin.setPublicationDate(LocalDate.now().minusDays(1));
                yesterdayBulletin.setPlaza(plaza);
                yesterdayBulletin.setCreatedBy(manager);
                bulletinRepository.save(yesterdayBulletin);
            }
        }
    }
}
