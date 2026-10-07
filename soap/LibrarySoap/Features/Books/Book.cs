using System.Runtime.Serialization;
using LibrarySoap.Shared;

namespace LibrarySoap.Features.Books;

/// <summary>Becomes the XSD complexType "Book" in the WSDL.</summary>
[DataContract(Namespace = Soap.Namespace)]
public class Book
{
    [DataMember(Order = 1)] public int Id { get; set; }
    [DataMember(Order = 2)] public string Title { get; set; } = "";
    [DataMember(Order = 3)] public int AuthorId { get; set; }
    [DataMember(Order = 4)] public int PublishingCompanyId { get; set; }
    [DataMember(Order = 5)] public int PublishingYear { get; set; }
}
