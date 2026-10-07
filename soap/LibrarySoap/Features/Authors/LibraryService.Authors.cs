using LibrarySoap.Features.Authors;
using LibrarySoap.Shared;

namespace LibrarySoap;

public partial class LibraryService
{
    public int CreateAuthor(string name, string surname) =>
        _authors.Create(Validate.Text(name, "name", 40), Validate.Text(surname, "surname", 60));

    public Author GetAuthorById(int id)
    {
        Validate.Id(id);
        return _authors.Get(id) ?? throw Fault.NotFound("Author", id);
    }

    public Author[] ListAuthors() => _authors.List();

    public bool UpdateAuthor(int id, string name, string surname)
    {
        Validate.Id(id);
        if (!_authors.Exists(id)) throw Fault.NotFound("Author", id);
        return _authors.Update(id, Validate.Text(name, "name", 40), Validate.Text(surname, "surname", 60));
    }

    public bool DeleteAuthor(int id)
    {
        Validate.Id(id);
        if (!_authors.Exists(id)) throw Fault.NotFound("Author", id);
        var books = _books.CountByAuthor(id);
        if (books > 0) throw Fault.Conflict($"Author {id} cannot be deleted: {books} book(s) still reference it.");
        return _authors.Delete(id);
    }
}
