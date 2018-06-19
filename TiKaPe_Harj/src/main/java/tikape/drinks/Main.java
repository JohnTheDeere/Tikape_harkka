package tikape.drinks;

import static java.lang.Integer.parseInt;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

public class Main {

    public static void main(String[] args) throws Exception {
        // asetetaan portti jos heroku antaa PORT-ympäristömuuttujan
        if (System.getenv("PORT") != null) {
            Spark.port(Integer.valueOf(System.getenv("PORT")));
        }

        System.out.println("Hello world!");
        
        // Lisätään home view
        Spark.get("/home", (req, res) -> {

            List<Annos> annokset = new ArrayList<>();

            // avaa yhteys tietokantaan
            Connection conn = getConnection();
            
            // tee kysely
            PreparedStatement stmt
                    = conn.prepareStatement("SELECT id, nimi FROM Annos");
            ResultSet tulos = stmt.executeQuery();

            // käsittele kyselyn tulokset
            while (tulos.next()) {
                String nimi = tulos.getString("nimi");
                Integer id = tulos.getInt("id");
                
                Annos annos_joku = new Annos(id, nimi);
                annokset.add(annos_joku);
            }
            // sulje yhteys tietokantaan
            conn.close();

            HashMap map = new HashMap<>();

            map.put("lista", annokset);

            return new ModelAndView(map, "home");
        }, new ThymeleafTemplateEngine());        
        
        // Lisätään drinks view missä listataan nykyiset juomat, ja missä voidaan
        // lisätä uusia juomia
            Spark.get("/drinks", (req, res) -> {

            List<Annos> annokset = new ArrayList<>();
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT id,nimi FROM Annos");
            ResultSet tulos = stmt.executeQuery();
            while (tulos.next()) {
                String nimi = tulos.getString("nimi");
                Integer id = tulos.getInt("id");
                Annos a = new Annos(id, nimi);
                annokset.add(a); 
            }
            
            List<Raakaaine> raakaaineet = new ArrayList<>();
            PreparedStatement stmt2 = conn.prepareStatement("SELECT id, nimi FROM Raakaaine");
            ResultSet tulos_raakaaine = stmt2.executeQuery();
            while (tulos_raakaaine.next()) {
                Integer id = tulos_raakaaine.getInt("id");
                String nimi = tulos_raakaaine.getString("nimi");
                Raakaaine r = new Raakaaine(id, nimi);
                raakaaineet.add(r);
            }
            conn.close();
            
            HashMap map = new HashMap<>();
            map.put("lista_yksi", annokset);
            map.put("lista_kaksi", raakaaineet);
            
            return new ModelAndView(map, "drinks");
        }, new ThymeleafTemplateEngine());
            
            
            
        Spark.post("/drinks", (req, res) -> {
    
            String annos = req.queryParams("annos");
            List<Annos> annos_oliot = new ArrayList<>();

            // avaa yhteys tietokantaan
            Connection conn1 = getConnection();

            //tee kysely
            PreparedStatement stmt1
                    = conn1.prepareStatement("SELECT id, nimi FROM Annos");
            ResultSet tulos = stmt1.executeQuery();

            // käsittele kyselyn tulokset
            while (tulos.next()) {
                Integer id = tulos.getInt("id");
                String nimi = tulos.getString("nimi");
                //raakaaineet.add(nimi);
                Annos annos_joku = new Annos(id, nimi);
                annos_oliot.add(annos_joku);
            }

            // Olemassaolevat raaka-aineet ovat nyt listassa raaka-aine_oliot
            // Laita testiarvo a = 0
            Integer a = 0;

            for (int i = 0; i < annos_oliot.size(); i++)            
                if (annos_oliot.get(i).getNimi().toUpperCase().equals(annos.toUpperCase()) || annos.trim().isEmpty()) {
                    a = 1;
                }

            if (a == 1) {
               conn1.close();
               stmt1.close();
               res.redirect("/drinks");
               return "";
            } else {

            // tee kysely
            PreparedStatement stmt2 = conn1.prepareStatement("INSERT INTO Annos"
                + " (nimi)"
                + " VALUES (?)");
            stmt2.setString(1, annos);
            stmt2.executeUpdate();

            stmt1.close();
            stmt2.close();
            conn1.close();
            
            res.redirect("/drinks");
            return "";

            }

        });    
                    //(annos_id, raakaaine_id, jarjestys, maara, ohje)"

        Spark.post("/drinks/:id/delete", (req, res) -> {
            Integer id_new = parseInt(req.params(":id"));
            Connection conn = getConnection();
            
            PreparedStatement stmt1 = conn.prepareStatement("DELETE FROM annosraakaaine WHERE annosraakaaine.annos_id = ?");
            stmt1.setInt(1, id_new);
            stmt1.executeUpdate();
            
            PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM Annos WHERE Annos.id = ?");   
            stmt2.setInt(1, id_new);
            stmt2.executeUpdate();
            res.redirect("/drinks");
            return "";
        });
            
        // Lisätään ingredients view missä listataan nykyiset raaka-aineet, ja missä voidaan
        // lisätä uusia raaka-aineita
        Spark.get("/ingredients", (req, res) -> {

            List<String> raakaaineet = new ArrayList<>();
            
            List<Raakaaine> raakaaineet_oliot = new ArrayList<>();

            // avaa yhteys tietokantaan
            Connection conn = getConnection();
            
            // tee kysely
            PreparedStatement stmt
                    = conn.prepareStatement("SELECT id, nimi FROM Raakaaine");
            ResultSet tulos = stmt.executeQuery();

            // käsittele kyselyn tulokset
            while (tulos.next()) {
                Integer id = tulos.getInt("id");
                String nimi = tulos.getString("nimi");

                Raakaaine raaka_aine_joku = new Raakaaine(id, nimi);
                raakaaineet_oliot.add(raaka_aine_joku);
            }
            // sulje yhteys tietokantaan
            // kommentti
            stmt.close();
            tulos.close();
            conn.close();

            HashMap map = new HashMap<>();

            //map.put("lista", raakaaineet);
            map.put("lista", raakaaineet_oliot);
            
            
                       
            
            
            return new ModelAndView(map, "ingredients");
        }, new ThymeleafTemplateEngine());
        
        
        Spark.post("/ingredients", (req, res) -> {
            String raakaaine = req.queryParams("raakaaine");
            
            // Tsekataan että lisättävä raaka-aine 1) ei ole tyhjä ja 2)
            // samaa raaka-ainetta ei olla lisätty ennen
            // Luetaan ensin kaikki jo lisätyt raaka-aineet listaan
            
            List<Raakaaine> raakaaineet_oliot = new ArrayList<>();

            // avaa yhteys tietokantaan
            Connection conn1 = getConnection();
            
            // tee kysely
            PreparedStatement stmt1
                    = conn1.prepareStatement("SELECT id, nimi FROM Raakaaine");
            ResultSet tulos = stmt1.executeQuery();

            // käsittele kyselyn tulokset
            while (tulos.next()) {
                Integer id = tulos.getInt("id");
                String nimi = tulos.getString("nimi");
                //raakaaineet.add(nimi);
                Raakaaine raaka_aine_joku = new Raakaaine(id, nimi);
                raakaaineet_oliot.add(raaka_aine_joku);
            }
            
                      
            // Olemassaolevat raaka-aineet ovat nyt listassa raaka-aine_oliot
            // Laita testiarvo a = 0
            Integer a = 0;

            
            // Looppaa kaikkien taskien läpi joita ei olla jaettu kenellekään
            // Jos syötetty :id = taskId löytyy listasta, sitä ei olla vielä lisätty
            // käyttäjälle, ja taski lisätään. Muussa tapauksessa palautetaan tyhjä sivu.
            for (int i = 0; i < raakaaineet_oliot.size(); i++)            
                if (raakaaineet_oliot.get(i).getNimi().toUpperCase().equals(raakaaine.toUpperCase()) || raakaaine.trim().isEmpty()) {
                    a = 1;
                }
         
            if (a == 1) {
               stmt1.close();
               conn1.close();
               res.redirect("/ingredients");
               return "";
            } else {
               // Tämän jälkeen toimii niinkuin sen pitää
               PreparedStatement stmt2 = conn1.prepareStatement("INSERT INTO Raakaaine"
                + " (nimi)"
                + " VALUES (?)");
                stmt2.setString(1, raakaaine);


                stmt2.executeUpdate();
                stmt1.close();
                stmt2.close();
                
               
                conn1.close();
                res.redirect("/ingredients");
                return "";
                
                
            }
            

        });
            
            
            
            
            
        // Poistetaan raaka-aine id mukaan
        Spark.post("/ingredients/:id/delete", (req, res) -> {
            Integer id_new = parseInt(req.params(":id"));
            
            Connection conn = getConnection();
            
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM annosraakaaine WHERE Raakaaine_id = ?");
            
            stmt.setInt(1, id_new);
            
            stmt.executeUpdate();
            
            stmt.close();
            
            conn.close();
            
            // Poista myös vastaava annosraakaaine
            Connection conn2 = getConnection();
            
            PreparedStatement stmt2 = conn2.prepareStatement("DELETE FROM Raakaaine WHERE Raakaaine.id = ?");
            
            System.out.println("Päästin tänne asti");
            
            stmt2.setInt(1, id_new);
            
            stmt2.executeUpdate();
            
            stmt2.close();
            
            conn2.close();
            
            
            
            
            
            res.redirect("/ingredients");
            
            return "";
        
        });
        
        Spark.post("/annosraakaaine", new Route() {
            @Override
            public Object handle(Request req, Response res) throws Exception {
                String annos = req.queryParams("annos");
                String raakaaine = req.queryParams("raakaaine");
                
                try{
                    Integer.valueOf(parseInt(req.queryParams("jarjestys")));
                    System.out.println("jarjestys on numero, ok");
                    
                    
                    Integer jarjestys = parseInt(req.queryParams("jarjestys"));
                    String maara = req.queryParams("maara");
                    String ohje = req.queryParams("ohje");
                
                
                    Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement("SELECT id FROM Annos WHERE Annos.nimi = ?");
                    stmt.setString(1, annos);
                    ResultSet tulos = stmt.executeQuery();
                    tulos.next();
                    Integer annos_id = tulos.getInt("id");
                    System.out.println(annos_id);
                    stmt.close();


                    PreparedStatement stmt2 = conn.prepareStatement("SELECT id FROM Raakaaine WHERE Raakaaine.nimi = ?");
                    stmt2.setString(1, raakaaine);
                    ResultSet tulos2 = stmt2.executeQuery();
                    tulos2.next();
                    Integer raakaaine_id = tulos2.getInt("id");
                    System.out.println(raakaaine_id);
                    stmt2.close();


                    PreparedStatement stmt3 = conn.prepareStatement("INSERT INTO annosraakaaine"
                            + " (annos_id, raakaaine_id, jarjestys, maara, ohje)"
                            + " VALUES (?,?,?,?,?)");
                    stmt3.setInt(1, annos_id);
                    stmt3.setInt(2, raakaaine_id);
                    stmt3.setInt(3, jarjestys);
                    stmt3.setString(4, maara);
                    stmt3.setString(5, ohje);

                    stmt3.executeUpdate();
                    stmt3.close();


                    conn.close();

                    res.redirect("/drinks");
                    return "";
                    
                } catch(NumberFormatException e){
                    System.out.println("järjestys EI numero, palauta tyhjä");
                    res.redirect("/drinks");
                    return "";
                }
                
                
                
            }
        });
        
        

        
        
        Spark.get("/drinks/:id", (req, res) -> {
            
             List<Raakaaine> raakaaineet_oliot = new ArrayList<>();

            // avaa yhteys tietokantaan
            Connection conn1 = getConnection();
            
            // tee kysely
            PreparedStatement stmt1
                    = conn1.prepareStatement("SELECT id, nimi FROM Raakaaine");
            ResultSet tulos = stmt1.executeQuery();

            // käsittele kyselyn tulokset
            while (tulos.next()) {
                Integer id = tulos.getInt("id");
                String nimi = tulos.getString("nimi");
                //raakaaineet.add(nimi);
                Raakaaine raaka_aine_joku = new Raakaaine(id, nimi);
                raakaaineet_oliot.add(raaka_aine_joku);
            }
            
            stmt1.close();
            tulos.close();
 
            // sulje yhteys tietokantaan
            conn1.close();
            

            List<AnnosRaakaaine> listaus = new ArrayList<>();
            
            HashMap map = new HashMap<>();
 
            Integer ID = Integer.parseInt(req.params(":id"));
            
            
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM annosraakaaine WHERE annosraakaaine.annos_id = ? ORDER BY annosraakaaine.jarjestys");
            stmt.setInt(1, ID);
            ResultSet rs = stmt.executeQuery();
       
                               
            while (rs.next()) {
            
            String asd = rs.getString("raakaaine_id");
      
            Raakaaine aine = raakaaineet_oliot.stream().filter(x -> x.getId() == Integer.parseInt(asd)).findFirst().get();
                
            AnnosRaakaaine a = new AnnosRaakaaine(aine.getNimi(), rs.getInt("annos_id"),
                rs.getInt("jarjestys"), rs.getString("maara"),
                rs.getString("ohje"));

            listaus.add(a);
        }
            
            
            map.put("lista", listaus);

            stmt.close();
            rs.close();
 
            // sulje yhteys tietokantaan
            conn.close();
            
            
            
            

            return new ModelAndView(map, "listaus");
        }, new ThymeleafTemplateEngine());
        
        
        Spark.get("/statistics", (req, res) -> {

            List<Raakaaine_laskuri> laskuri = new ArrayList<>();
            
            Connection conn2 = getConnection();
            PreparedStatement stmt2
                    = conn2.prepareStatement("select raakaaine.nimi as nimi, count(*) as lasku from annosraakaaine LEFT JOIN raakaaine ON annosraakaaine.raakaaine_id = raakaaine.id GROUP BY raakaaine.nimi");
           
            ResultSet tulos2 = stmt2.executeQuery();
            while (tulos2.next()) {
                String nimi = tulos2.getString("nimi");
                Integer lasku = tulos2.getInt("lasku");
                
                Raakaaine_laskuri laskuri_olio = new Raakaaine_laskuri(lasku, nimi);
                laskuri.add(laskuri_olio);
                
                
            }
            

            stmt2.close();
            tulos2.close();
            conn2.close();
            
            HashMap map = new HashMap<>();
            
            map.put("laskurilista", laskuri);

           

            return new ModelAndView(map, "statistics");
        }, new ThymeleafTemplateEngine()); 
        
        
       
       
    }

    public static Connection getConnection() throws Exception {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        if (dbUrl != null && dbUrl.length() > 0) {
            return DriverManager.getConnection(dbUrl);
        }

        return DriverManager.getConnection("jdbc:sqlite:drinkit.db");
    }

}
