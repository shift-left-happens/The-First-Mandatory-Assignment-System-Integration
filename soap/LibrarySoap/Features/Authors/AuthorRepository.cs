using Dapper;
using LibrarySoap.Shared;

namespace LibrarySoap.Features.Authors;

/// <summary>SQL for the tauthor table. Legacy column names are aliased to our contract names.</summary>
public class AuthorRepository(Db db)
{
    private const string Select = "SELECT nAuthorID AS Id, cName AS Name, COALESCE(cSurname, '') AS Surname FROM tauthor";

    public Author? Get(int id)
    {
        using var c = db.Open();
        return c.QuerySingleOrDefault<Author>($"{Select} WHERE nAuthorID = @id", new { id });
    }

    public Author[] List()
    {
        using var c = db.Open();
        return c.Query<Author>($"{Select} ORDER BY nAuthorID").ToArray();
    }

    public bool Exists(int id)
    {
        using var c = db.Open();
        return c.ExecuteScalar<bool>("SELECT EXISTS(SELECT 1 FROM tauthor WHERE nAuthorID = @id)", new { id });
    }

    public int Create(string name, string surname)
    {
        using var c = db.Open();
        return c.ExecuteScalar<int>(
            "INSERT INTO tauthor (cName, cSurname) VALUES (@name, @surname); SELECT last_insert_rowid();",
            new { name, surname });
    }

    public bool Update(int id, string name, string surname)
    {
        using var c = db.Open();
        return c.Execute("UPDATE tauthor SET cName = @name, cSurname = @surname WHERE nAuthorID = @id",
            new { id, name, surname }) > 0;
    }

    public bool Delete(int id)
    {
        using var c = db.Open();
        return c.Execute("DELETE FROM tauthor WHERE nAuthorID = @id", new { id }) > 0;
    }
}
