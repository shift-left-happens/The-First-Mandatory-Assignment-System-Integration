using Microsoft.Data.Sqlite;

namespace LibrarySoap;

/// <summary>
/// Opens connections to a local working copy of the shared SQLite database.
/// On first use, Database:Source (the tracked database/library.db) is copied to Database:WorkingCopy
/// (gitignored). All reads and writes go to the copy, so the tracked file never changes.
/// Delete the copy to reset to the original data.
/// </summary>
public class Db
{
    private readonly string _connectionString;

    public Db(IConfiguration config, IHostEnvironment env, ILogger<Db> logger)
    {
        // Relative paths are resolved from the project folder, so it works no matter where you start the app from.
        var source = Path.GetFullPath(Path.Combine(env.ContentRootPath, config["Database:Source"]!));
        var workingCopy = Path.GetFullPath(Path.Combine(env.ContentRootPath, config["Database:WorkingCopy"]!));

        if (!File.Exists(workingCopy))
        {
            File.Copy(source, workingCopy);
            logger.LogInformation("Created working copy {WorkingCopy} from {Source}", workingCopy, source);
        }
        logger.LogInformation("Using database {WorkingCopy} (delete it to reset to the original data)", workingCopy);

        _connectionString = new SqliteConnectionStringBuilder { DataSource = workingCopy }.ToString();
    }

    public SqliteConnection Open()
    {
        var connection = new SqliteConnection(_connectionString);
        connection.Open();
        return connection;
    }
}
