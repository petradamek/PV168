package cz.muni.fi.pv168.books;

/**
 * Customer entity. A JavaBean.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Customer {

    private Long id;
    private String fullname;
    private String address;
    private String phone;
    private String email;

    public Customer() {
    }

    public Customer(Long id, String fullname, String address, String phone, String email) {
        this.id = id;
        this.fullname = fullname;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", fullname='" + fullname + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Customer customer = (Customer) o;

        if (address != null ? !address.equals(customer.address) : customer.address != null) return false;
        if (email != null ? !email.equals(customer.email) : customer.email != null) return false;
        if (fullname != null ? !fullname.equals(customer.fullname) : customer.fullname != null) return false;
        if (id != null ? !id.equals(customer.id) : customer.id != null) return false;
        if (phone != null ? !phone.equals(customer.phone) : customer.phone != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (fullname != null ? fullname.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}
