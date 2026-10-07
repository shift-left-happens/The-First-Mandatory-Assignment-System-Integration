using Dapper;
using LibrarySoap.Shared;

namespace LibrarySoap.Features.Books;

/// <summary>SQL for the tbook table. Legacy column names are aliased to our contract names.</summary>
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
