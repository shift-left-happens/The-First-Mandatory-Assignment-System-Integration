using Microsoft.Data.Sqlite;

namespace LibrarySoap.Shared;

/// <summary>Opens connections to the shared SQLite database (path from appsettings.json).</summary>
public class Db
{
    private readonly string _connectionString;

    public Db(IConfiguration config, IHostEnvironment env)
    {
        var builder = new SqliteConnectionStringBuilder(config.GetConnectionString("Library"));
        // A relative path is resolved from the project folder, so it works no matter where you start the app from.
        builder.DataSource = Path.GetFullPath(Path.Combine(env.ContentRootPath, builder.DataSource));
        _connectionString = builder.ToString();
    }

    public SqliteConnection Open()
    {
        var connection = new SqliteConnection(_connectionString);
        connection.Open();
        return connection;
    }
}
