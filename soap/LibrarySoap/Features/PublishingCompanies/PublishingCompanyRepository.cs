using Dapper;
using LibrarySoap.Shared;

namespace LibrarySoap.Features.PublishingCompanies;

/// <summary>SQL for the tpublishingcompany table. Legacy column names are aliased to our contract names.</summary>
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
