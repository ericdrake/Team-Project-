package edu.jsu.mcis.cs310.tas_sp22;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;



public class TASDatabase {
    private final Connection connection;

    public TASDatabase(String username, String password, String address){
        this.connection = openConnection(username, password, address);
    }

    public boolean isConnected(){
        boolean connected = false;
        try {
            if( !(connection == null))
                connected = !(connection.isClosed());
        }
        catch (SQLException e) {e.printStackTrace();}
        return connected;
    }

    private Connection openConnection(String u, String p, String a){
        Connection conn = null;

        if(a.equals("") || u.equals("") || p.equals(""))
            System.err.println("ERROR: PLEASE SPECIFY THE ADDRESS/USERNAME/PASSWORD BEFORE OPENING THE DATABASE CONNECTION ");
        else {
            try {
                String url = "jdbc:mysql://" + a + "/tas_sp22_v1?autoReconnect=true&useSSL=false&zeroDateTimeBehavior=EXCEPTION&serverTimezone=America/Chicago";
                // System.err.println("Connecting to " + url + "...");
                conn = DriverManager.getConnection(url,u,p);
            }
            catch (Exception e){e.printStackTrace();}
        }
        return conn;
    }

    public Badge getBadge(String id) {

        Badge b = null;

        try {

            PreparedStatement pstUpdate = null, pstSelect = null;
            ResultSet resultset = null;

            String query = "SELECT * FROM badge WHERE id = ?";
            pstSelect = connection.prepareStatement(query);
            pstSelect.setString(1, id);


            pstSelect.execute();

            resultset = pstSelect.getResultSet();

            if(resultset.next()) {
                String desc = resultset.getString("description");
                b = new Badge(id, desc);

            }

        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return b;
    }
    
    //Method to get Employee data using the integer id value
    public Employee getEmployee(int id) {
        Employee employee = null;

        try {
            String query = "SELECT * FROM employee WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id);

            ps.execute();

            ResultSet rS = ps.getResultSet();


            if (rS.next()){
                HashMap<String, String> hp = new HashMap<>();

                hp.put("id", String.valueOf(id));
                hp.put("badgeid", rS.getString("badgeid") );
                hp.put("firstname", rS.getString("firstname"));
                hp.put("middlename", rS.getString("middlename"));
                hp.put("lastname", rS.getString("lastname"));
                hp.put("employeetypeid", String.valueOf(rS.getInt("employeetypeid")));
                hp.put("departmentid", String.valueOf(rS.getInt("departmentid")));
                hp.put("shiftid", String.valueOf(rS.getInt("shiftid")));
                hp.put("active", rS.getTimestamp("active").toLocalDateTime().toString());

                java.sql.Timestamp inactive = rS.getTimestamp("inactive");

                if (inactive != null)

                    hp.put("inactive", inactive.toLocalDateTime().toString());


                employee = new Employee(hp);
            }
        }

        catch(SQLException e){e.printStackTrace();}
        return employee;
    }
    
    //Method to get Employee record using the badgeid value from the Badge class
    public Employee getEmployee(Badge badgeid){
           Employee employee = null;

        try {
            String query = "SELECT * FROM employee WHERE badgeid = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, badgeid.getId());

            ps.execute();

             ResultSet rS = ps.getResultSet();

            if (rS.next()){
                int id = rS.getInt("id");
                employee = getEmployee(id);
            }
        }

        catch(SQLException e){e.printStackTrace();}
        return employee;
    }


    public Punch getPunch(int id) {

        Punch punch = null;

        try {

            PreparedStatement pstUpdate = null, pstSelect = null;
            ResultSet resultset = null;

            String query = "SELECT * FROM event WHERE id = ?";
            pstSelect = connection.prepareStatement(query);
            pstSelect.setInt(1, id);


            pstSelect.execute();

            resultset = pstSelect.getResultSet();

            if(resultset.next()) {
                HashMap<String, String> parameters = new  HashMap<String,String>();
                
                parameters.put("id", String.valueOf(id));
                parameters.put("terminalid", String.valueOf(resultset.getInt("terminalid")));
                parameters.put("badgeid", resultset.getString("badgeid"));
                parameters.put("punchtypeid", String.valueOf(resultset.getInt("eventtypeid")));

                Timestamp timestamp = resultset.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                parameters.put("timestamp", String.valueOf(dateTime));
                
                punch = new Punch(parameters);


            }

        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return punch;
    }
    
    // method to receive the shift for given shift id
    public Shift getShift(int id) {
        Shift shift = null;

        try {

            // query created to retrieve shift by id
            String query = "SELECT * FROM shift WHERE id = ?";
            PreparedStatement pstSelect = connection.prepareStatement(query);
            pstSelect.setInt(1, id);
            // execute the query
            boolean pstSelectExe = pstSelect.execute();

            if (pstSelectExe) {
                ResultSet resultset = pstSelect.getResultSet();
                // check the result set
                if (resultset.next()) {
                    // obtain the column values from shift table
                    HashMap<String, String> params = new HashMap<String, String>();

                    params.put("description", resultset.getString("description"));

                    params.put("id", String.valueOf(resultset.getInt("id")));

                    params.put("ShiftStart", resultset.getTime("ShiftStart").toLocalTime().toString());

                    params.put("ShiftStop", resultset.getTime("ShiftStop").toLocalTime().toString());

                    params.put("roundinterval", String.valueOf(resultset.getInt("roundinterval")));

                    params.put("graceperiod", String.valueOf(resultset.getInt("graceperiod")));

                    params.put("dockpenalty", String.valueOf(resultset.getInt("dockpenalty")));

                    params.put("LunchStart", resultset.getTime("LunchStart").toLocalTime().toString());

                    params.put("LunchStop", resultset.getTime("LunchStop").toLocalTime().toString());

                    params.put("lunchthreshold", String.valueOf(resultset.getInt("lunchthreshold")));
                    shift = new Shift(params);

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // return shift object
        return shift;
    }

    // method to receive the shift for given shift badge
    public Shift getShift(Badge badge) {
        Shift shift = null;

        try {
            //declare necessary variables
            PreparedStatement pstSelect = null;
            ResultSet resultset = null;

            // query created to retrieve shift by badge
            String query = "SELECT s.* FROM shift as s, employee as e, badge as b WHERE e.badgeid = ? AND e.shiftid=s.id";
            pstSelect = connection.prepareStatement(query);
            pstSelect.setString(1, badge.getId());
            // execute the query
            boolean pstSelectExe = pstSelect.execute();

            if (pstSelectExe) {
                resultset = pstSelect.getResultSet();
                // check the result set
                if (resultset.next()) {
                    // obtain the column values from shift table
                    HashMap<String, String> params = new HashMap<String, String>();

                    params.put("description", resultset.getString("description"));

                    params.put("id", String.valueOf(resultset.getInt("id")));

                    params.put("ShiftStart", resultset.getTime("ShiftStart").toLocalTime().toString());

                    params.put("ShiftStop", resultset.getTime("ShiftStop").toLocalTime().toString());

                    params.put("roundinterval", String.valueOf(resultset.getInt("roundinterval")));

                    params.put("graceperiod", String.valueOf(resultset.getInt("graceperiod")));

                    params.put("dockpenalty", String.valueOf(resultset.getInt("dockpenalty")));

                    params.put("LunchStart", resultset.getTime("LunchStart").toLocalTime().toString());

                    params.put("LunchStop", resultset.getTime("LunchStop").toLocalTime().toString());

                    params.put("lunchthreshold", String.valueOf(resultset.getInt("lunchthreshold")));
                    shift = new Shift(params);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // return shift object
        return shift;
    }
    
        public Department getDepartment(int id) {

        Department d = null;

        try {

            PreparedStatement pstSelect = null;
            ResultSet resultset = null;

            String query = "SELECT * FROM department WHERE id = ?";
            pstSelect = connection.prepareStatement(query);
            pstSelect.setInt(1, id);


            pstSelect.execute();

            resultset = pstSelect.getResultSet();

            if(resultset.next()) {
                String desc = resultset.getString("description");
                int terminalId = resultset.getInt("terminalid");
                d = new Department(id, desc, terminalId);

            }

        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return d;
    }
        
        //Insert Punch 
    public int insertPunch(Punch p) {
        
        int result = 0;
        
        //Getting the punch values needed
        int terminalidPunch = p.getTerminalid();
        Badge badgeid = getBadge(p.getBadgeid());
        int eventtypeid = p.getPunchtype().ordinal();
        
        //Getting the Department and Employee values needed
        Employee e1 = getEmployee(badgeid);
        int departmentid = e1.getDepartmentid();
        int terminalidDept = getDepartment(departmentid).getTerminalId();
        
        if (terminalidPunch == terminalidDept || terminalidPunch == 0) {
        
            try {
                
                String query = "INSERT INTO event (terminalid, badgeid, timestamp, eventtypeid) VALUES (?,?,?,?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, terminalidPunch);
                ps.setString(2, badgeid.toString());
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(p.getTimestamp()));
                ps.setInt(4, eventtypeid);
                
                result = ps.executeUpdate();
            }
            
            catch(Exception e){e.printStackTrace();}
        
        }
        
          

        return result;
        
     }
    
    public ArrayList<Punch> getDailyPunchList(Badge badge, LocalDate date){
        ArrayList<Punch> arrayList = new ArrayList<Punch>();
//        arrayList = null;
        try {

            PreparedStatement pstSelect = null;
            ResultSet resultset = null;


            String query = "SELECT *, DATE(`timestamp`) AS tsdate FROM event WHERE badgeid = ? HAVING tsdate = ?";
//             Fetch all columns then get localDate from timestamp then filter using the timestamp
            pstSelect = connection.prepareStatement(query);
            pstSelect.setString(1, badge.getId());
            pstSelect.setString(2, date.toString());

            pstSelect.execute();

            resultset = pstSelect.getResultSet();

            while(resultset.next()) {
                
                int id = resultset.getInt("id");
                //Add punch to the arraylist
                arrayList.add(getPunch(id));
                
            }

        }

        catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println(arrayList.toString());
        return arrayList;
        
    }

}
