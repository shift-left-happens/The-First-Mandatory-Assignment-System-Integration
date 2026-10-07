using Dapper;

namespace LibrarySoap;

// SQL only, one class per table. The legacy column names (nBookID, cTitle, ...) are aliased
// to our contract names so Dapper can map them straight onto Book / Author / PublishingCompany.

public class BookRepository(Db db)
{
    private const string Select =
        "SELECT nBookID AS Id, cTitle AS Title, nAuthorID AS AuthorId, nPublishingCompanyID AS PublishingCompanyId, " +
        "CAST(nPublishingYear AS INTEGER) AS PublishingYear FROM tbook";

    public Book? Get(int id)
    {
        using var c = db.Open();
        return c.QuerySingleOrDefault<Book>($"{Select} WHERE nBookID = @id", new { id });
    }

    public int Create(string title, int authorId, int publishingCompanyId, int publishingYear)
    {
        using var c = db.Open();
        return c.ExecuteScalar<int>(
            "INSERT INTO tbook (cTitle, nAuthorID, nPublishingCompanyID, nPublishingYear) " +
            "VALUES (@title, @authorId, @publishingCompanyId, @publishingYear); SELECT last_insert_rowid();",
            new { title, authorId, publishingCompanyId, publishingYear });
    }

    public bool Update(int id, string title, int authorId, int publishingCompanyId, int publishingYear)
    {
        using var c = db.Open();
        return c.Execute(
            "UPDATE tbook SET cTitle = @title, nAuthorID = @authorId, nPublishingCompanyID = @publishingCompanyId, " +
            "nPublishingYear = @publishingYear WHERE nBookID = @id",
            new { id, title, authorId, publishingCompanyId, publishingYear }) > 0;
    }

    public bool Delete(int id)
    {
        using var c = db.Open();
        return c.Execute("DELETE FROM tbook WHERE nBookID = @id", new { id }) > 0;
    }

    public int CountByAuthor(int authorId)
    {
        using var c = db.Open();
        return c.ExecuteScalar<int>("SELECT COUNT(*) FROM tbook WHERE nAuthorID = @authorId", new { authorId });
    }

    public int CountByPublishingCompany(int publishingCompanyId)
    {
        using var c = db.Open();
        return c.ExecuteScalar<int>("SELECT COUNT(*) FROM tbook WHERE nPublishingCompanyID = @publishingCompanyId", new { publishingCompanyId });
    }
}

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

public class PublishingCompanyRepository(Db db)
{
    private const string Select = "SELECT nPublishingCompanyID AS Id, cName AS Name FROM tpublishingcompany";

    public PublishingCompany? Get(int id)
    {
        using var c = db.Open();
        return c.QuerySingleOrDefault<PublishingCompany>($"{Select} WHERE nPublishingCompanyID = @id", new { id });
    }

    public PublishingCompany[] List()
    {
        using var c = db.Open();
        return c.Query<PublishingCompany>($"{Select} ORDER BY nPublishingCompanyID").ToArray();
    }

    public bool Exists(int id)
    {
        using var c = db.Open();
        return c.ExecuteScalar<bool>("SELECT EXISTS(SELECT 1 FROM tpublishingcompany WHERE nPublishingCompanyID = @id)", new { id });
    }

    public int Create(string name)
    {
        using var c = db.Open();
        return c.ExecuteScalar<int>(
            "INSERT INTO tpublishingcompany (cName) VALUES (@name); SELECT last_insert_rowid();", new { name });
    }

    public bool Update(int id, string name)
    {
        using var c = db.Open();
        return c.Execute("UPDATE tpublishingcompany SET cName = @name WHERE nPublishingCompanyID = @id", new { id, name }) > 0;
    }

    public bool Delete(int id)
    {
        using var c = db.Open();
        return c.Execute("DELETE FROM tpublishingcompany WHERE nPublishingCompanyID = @id", new { id }) > 0;
    }
}
