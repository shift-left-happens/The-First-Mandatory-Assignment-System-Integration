using LibrarySoap.Features.PublishingCompanies;
using LibrarySoap.Shared;

namespace LibrarySoap;

public partial class LibraryService
{
    public int CreatePublishingCompany(string name) =>
        _publishers.Create(Validate.Text(name, "name", 40));

    public PublishingCompany GetPublishingCompanyById(int id)
    {
        Validate.Id(id);
        return _publishers.Get(id) ?? throw Fault.NotFound("PublishingCompany", id);
    }

    public PublishingCompany[] ListPublishingCompanies() => _publishers.List();

    public bool UpdatePublishingCompany(int id, string name)
    {
        Validate.Id(id);
        if (!_publishers.Exists(id)) throw Fault.NotFound("PublishingCompany", id);
        return _publishers.Update(id, Validate.Text(name, "name", 40));
    }

    public bool DeletePublishingCompany(int id)
    {
        Validate.Id(id);
        if (!_publishers.Exists(id)) throw Fault.NotFound("PublishingCompany", id);
        var books = _books.CountByPublishingCompany(id);
        if (books > 0) throw Fault.Conflict($"Publishing company {id} cannot be deleted: {books} book(s) still reference it.");
        return _publishers.Delete(id);
    }
}
