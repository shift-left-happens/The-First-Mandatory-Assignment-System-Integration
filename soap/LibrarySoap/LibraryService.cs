using CoreWCF;
using LibrarySoap.Features.Authors;
using LibrarySoap.Features.Books;
using LibrarySoap.Features.PublishingCompanies;
using LibrarySoap.Shared;

namespace LibrarySoap;

/// <summary>
/// Implementation of the contract. Also <c>partial</c>: the operations live in Features/*/LibraryService.*.cs.
/// A new instance is created per request (registered as transient in Program.cs).
/// </summary>
[ServiceBehavior(Namespace = Soap.Namespace)]
public partial class LibraryService : ILibraryService
{
    private readonly BookRepository _books;
    private readonly AuthorRepository _authors;
    private readonly PublishingCompanyRepository _publishers;

    public LibraryService(BookRepository books, AuthorRepository authors, PublishingCompanyRepository publishers)
    {
        _books = books;
        _authors = authors;
        _publishers = publishers;
    }
}
