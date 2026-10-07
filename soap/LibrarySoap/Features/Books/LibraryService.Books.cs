using LibrarySoap.Features.Books;
using LibrarySoap.Shared;

namespace LibrarySoap;

public partial class LibraryService
{
    private const int MinPublishingYear = 1900;

    public int CreateBook(string title, int authorId, int publishingCompanyId, int publishingYear)
    {
        title = ValidateBook(title, authorId, publishingCompanyId, publishingYear);
        return _books.Create(title, authorId, publishingCompanyId, publishingYear);
    }

    public Book GetBookById(int id)
    {
        Validate.Id(id);
        return _books.Get(id) ?? throw Fault.NotFound("Book", id);
    }

    public bool UpdateBook(int id, string title, int authorId, int publishingCompanyId, int publishingYear)
    {
        Validate.Id(id);
        if (_books.Get(id) is null) throw Fault.NotFound("Book", id);
        title = ValidateBook(title, authorId, publishingCompanyId, publishingYear);
        return _books.Update(id, title, authorId, publishingCompanyId, publishingYear);
    }

    public bool DeleteBook(int id)
    {
        Validate.Id(id);
        if (!_books.Delete(id)) throw Fault.NotFound("Book", id);
        return true;
    }

    /// <summary>Checks all book fields, including that the author and publisher exist. Returns the trimmed title.</summary>
    private string ValidateBook(string title, int authorId, int publishingCompanyId, int publishingYear)
    {
        title = Validate.Text(title, "title", 255);
        Validate.Id(authorId, "authorId");
        Validate.Id(publishingCompanyId, "publishingCompanyId");
        if (publishingYear < MinPublishingYear)
            throw Fault.Validation("publishingYear", $"'publishingYear' must be {MinPublishingYear} or later.");
        // The database has no foreign keys, so the references are checked here.
        if (!_authors.Exists(authorId))
            throw Fault.Validation("authorId", $"Author {authorId} does not exist.");
        if (!_publishers.Exists(publishingCompanyId))
            throw Fault.Validation("publishingCompanyId", $"Publishing company {publishingCompanyId} does not exist.");
        return title;
    }
}
