package lab.db.tables;

 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.SQLException;
 import java.sql.SQLIntegrityConstraintViolationException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Objects;
 import java.util.Optional;

import com.mysql.cj.protocol.Resultset;

import lab.utils.Utils;
 import lab.db.Table;
 import lab.model.Student;

 public final class StudentsTable implements Table<Student, Integer> {    
     public static final String TABLE_NAME = "students";

     private final Connection connection; 

     public StudentsTable(final Connection connection) {
         this.connection = Objects.requireNonNull(connection);
     }

     @Override
     public String getTableName() {
         return TABLE_NAME;
     }

     @Override
     public boolean createTable() {
         // 1. Create the statement from the open connection inside a try-with-resources
         try (final Statement statement = this.connection.createStatement()) {
             // 2. Execute the statement with the given query
             statement.executeUpdate(
                 "CREATE TABLE " + TABLE_NAME + " (" +
                         "id INT NOT NULL PRIMARY KEY," +
                         "firstName CHAR(40) NOT NULL," + 
                         "lastName CHAR(40) NOT NULL," + 
                         "birthday DATE" + 
                     ")");
             return true;
         } catch (final SQLException e) {
             // 3. Handle possible SQLExceptions
             return false;
         }
     }

     @Override
     public Optional<Student> findByPrimaryKey(final Integer id) {
         final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?" ;
         try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            final ResultSet result = statement.executeQuery();
            return readStudentsFromResultSet(result).stream().findFirst();
         } catch (SQLException e) {
            throw new IllegalStateException(e);
         }
     }

     /**
      * Given a ResultSet read all the students in it and collects them in a List
      * @param resultSet a ResultSet from which the Student(s) will be extracted
      * @return a List of all the students in the ResultSet
      */
     private List<Student> readStudentsFromResultSet(final ResultSet resultSet) {
        List<Student> list = new ArrayList<>();
        try {
            while(resultSet.next()) {
                list.add(new Student(resultSet.getInt("id"),resultSet.getString("firstname"), resultSet.getString("lastname"),
                    Optional.ofNullable(Utils.sqlDateToDate(resultSet.getDate("birthday")))));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return list;
         // Create an empty list, then
         // Inside a loop you should:
         //      1. Call resultSet.next() to advance the pointer and check there are still rows to fetch
         //      2. Use the getter methods to get the value of the columns
         //      3. After retrieving all the data create a Student object
         //      4. Put the student in the List
         // Then return the list with all the found students

         // Helpful resources:
         // https://docs.oracle.com/javase/7/docs/api/java/sql/ResultSet.html
         // https://docs.oracle.com/javase/tutorial/jdbc/basics/retrieving.html
     }

     @Override
     public List<Student> findAll() {
        final String query = "SELECT * FROM " + TABLE_NAME + ";";
         try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            final ResultSet result = statement.executeQuery();
            return readStudentsFromResultSet(result);
         } catch (SQLException e) {
            throw new IllegalStateException(e);
         }
     }

     public List<Student> findByBirthday(final Date date) {
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE birthday = ?" ;
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
           statement.setObject(1, Utils.dateToSqlDate(date));
           final ResultSet result = statement.executeQuery();
           return readStudentsFromResultSet(result);
        } catch (SQLException e) {
           throw new IllegalStateException(e);
        }
     }

     @Override
     public boolean dropTable() {
        final String query = "DROP TABLE " + TABLE_NAME + ";";
        try(final Statement statement = connection.createStatement()){
            statement.executeUpdate(query);
            return true;
        }catch(SQLException e) {
            //throw new IllegalStateException();
            return false;
        }
     }

     @Override
     public boolean save(final Student student) {
        final String query = "INSERT INTO students values (?,?,?,?);";
        try(final PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, student.getId());
            statement.setString(2, student.getFirstName());
            statement.setString(3, student.getLastName());
            statement.setDate(4, student.getBirthday().isPresent() ? Utils.dateToSqlDate(student.getBirthday().get()) : null);
            statement.executeUpdate();
            return true;
        }catch(SQLException e) {
            return false;
        }
     }

     @Override
     public boolean delete(final Integer id) {
        String query = "DELETE FROM " +  TABLE_NAME + " WHERE id = ?;";
        try(final PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, id);
            return statement.executeUpdate() > 0 ? true : false;
        }catch (SQLException e) {
            return false;
        }
     }

     @Override
     public boolean update(final Student student) {
        String query = "UPDATE " + TABLE_NAME + " SET firstname = ?, lastname = ?, birthday = ? WHERE id = ?;";
        try(final PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, student.getFirstName());
            statement.setString(2, student.getLastName());
            statement.setDate(3, student.getBirthday().isPresent() ? Utils.dateToSqlDate(student.getBirthday().get()) : null);
            statement.setInt(4, student.getId());
            return statement.executeUpdate() > 0 ? true : false;
        }catch (SQLException e) {
            return false;
        }
     }
 }