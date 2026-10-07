using CoreWCF;

namespace LibrarySoap;

/// <summary>
/// Implementation of the contract: validation and business rules. SQL lives in Repositories.cs.
/// A new instance is created per request (registered as transient in Program.cs).
/// </summary>
[ServiceBehavior(Namespace = Soap.Namespace)]
public class LibraryService(BookRepository books, AuthorRepository authors, PublishingCompanyRepository publishers)
    : ILibraryService
{
    private const int MinPublishingYear = 1900;

    // ---- Books ----

    public int CreateBook(string title, int authorId, int publishingCompanyId, int publishingYear)
    {
        title = ValidateBook(title, authorId, publishingCompanyId, publishingYear);
        return books.Create(title, authorId, publishingCompanyId, publishingYear);
    }

    public Book GetBookById(int id)
    {
        ValidateId(id);
        return books.Get(id) ?? throw Fault.NotFound("Book", id);
    }

    public bool UpdateBook(int id, string title, int authorId, int publishingCompanyId, int publishingYear)
    {
        ValidateId(id);
        if (books.Get(id) is null) throw Fault.NotFound("Book", id);
        title = ValidateBook(title, authorId, publishingCompanyId, publishingYear);
        return books.Update(id, title, authorId, publishingCompanyId, publishingYear);
    }

    public bool DeleteBook(int id)
    {
        ValidateId(id);
        if (!books.Delete(id)) throw Fault.NotFound("Book", id);
        return true;
    }

    // ---- Authors ----

    public int CreateAuthor(string name, string surname) =>
        authors.Create(ValidateText(name, "name", 40), ValidateText(surname, "surname", 60));

    public Author GetAuthorById(int id)
    {
        ValidateId(id);
        return authors.Get(id) ?? throw Fault.NotFound("Author", id);
    }

    public Author[] ListAuthors() => authors.List();

    public bool UpdateAuthor(int id, string name, string surname)
    {
        ValidateId(id);
        if (!authors.Exists(id)) throw Fault.NotFound("Author", id);
        return authors.Update(id, ValidateText(name, "name", 40), ValidateText(surname, "surname", 60));
    }

    public bool DeleteAuthor(int id)
    {
        ValidateId(id);
        if (!authors.Exists(id)) throw Fault.NotFound("Author", id);
        var count = books.CountByAuthor(id);
        if (count > 0) throw Fault.Conflict($"Author {id} cannot be deleted: {count} book(s) still reference it.");
        return authors.Delete(id);
    }

    // ---- Publishing companies ----

    public int CreatePublishingCompany(string name) =>
        publishers.Create(ValidateText(name, "name", 40));

    public PublishingCompany GetPublishingCompanyById(int id)
    {
        ValidateId(id);
        return publishers.Get(id) ?? throw Fault.NotFound("PublishingCompany", id);
    }

    public PublishingCompany[] ListPublishingCompanies() => publishers.List();

    public bool UpdatePublishingCompany(int id, string name)
    {
        ValidateId(id);
        if (!publishers.Exists(id)) throw Fault.NotFound("PublishingCompany", id);
        return publishers.Update(id, ValidateText(name, "name", 40));
    }

    public bool DeletePublishingCompany(int id)
    {
        ValidateId(id);
        if (!publishers.Exists(id)) throw Fault.NotFound("PublishingCompany", id);
        var count = books.CountByPublishingCompany(id);
        if (count > 0) throw Fault.Conflict($"Publishing company {id} cannot be deleted: {count} book(s) still reference it.");
        return publishers.Delete(id);
    }

    // ---- Validation (each check throws a ValidationFault when it fails) ----

    /// <summary>Checks all book fields, including that the author and publisher exist. Returns the trimmed title.</summary>
    private string ValidateBook(string title, int authorId, int publishingCompanyId, int publishingYear)
    {
        title = ValidateText(title, "title", 255);
        ValidateId(authorId, "authorId");
        ValidateId(publishingCompanyId, "publishingCompanyId");
        if (publishingYear < MinPublishingYear)
            throw Fault.Validation("publishingYear", $"'publishingYear' must be {MinPublishingYear} or later.");
        // The database has no foreign keys, so the references are checked here.
        if (!authors.Exists(authorId))
            throw Fault.Validation("authorId", $"Author {authorId} does not exist.");
        if (!publishers.Exists(publishingCompanyId))
            throw Fault.Validation("publishingCompanyId", $"Publishing company {publishingCompanyId} does not exist.");
        return title;
    }

    /// <summary>Required, non-blank text up to <paramref name="maxLength"/> characters. Returns the trimmed value.</summary>
    private static string ValidateText(string? value, string field, int maxLength)
    {
        var trimmed = value?.Trim();
        if (string.IsNullOrEmpty(trimmed)) throw Fault.Validation(field, $"'{field}' is required.");
        if (trimmed.Length > maxLength) throw Fault.Validation(field, $"'{field}' must be at most {maxLength} characters.");
        return trimmed;
    }

    /// <summary>Ids must be positive. A missing element in the XML arrives as 0 and fails here.</summary>
    private static void ValidateId(int id, string field = "id")
    {
        if (id <= 0) throw Fault.Validation(field, $"'{field}' must be a positive integer.");
    }
}
