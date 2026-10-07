using System.Runtime.Serialization;

namespace LibrarySoap;

// Data contracts. Each class becomes an XSD complexType in the WSDL.

[DataContract(Namespace = Soap.Namespace)]
public class Book
{
    [DataMember(Order = 1)] public int Id { get; set; }
    [DataMember(Order = 2)] public string Title { get; set; } = "";
    [DataMember(Order = 3)] public int AuthorId { get; set; }
    [DataMember(Order = 4)] public int PublishingCompanyId { get; set; }
    [DataMember(Order = 5)] public int PublishingYear { get; set; }
}

[DataContract(Namespace = Soap.Namespace)]
public class Author
{
    [DataMember(Order = 1)] public int Id { get; set; }
    [DataMember(Order = 2)] public string Name { get; set; } = "";
    [DataMember(Order = 3)] public string Surname { get; set; } = "";
}

[DataContract(Namespace = Soap.Namespace)]
public class PublishingCompany
{
    [DataMember(Order = 1)] public int Id { get; set; }
    [DataMember(Order = 2)] public string Name { get; set; } = "";
}
