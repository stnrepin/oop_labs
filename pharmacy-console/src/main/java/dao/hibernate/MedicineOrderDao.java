package dao.hibernate;

import java.time.Instant;
import java.util.List;

import models.Medicine;
import models.MedicineOrder;
import utils.PersistenceEntityManagerUtils;

/**
 * {@inheritDoc}
 */
public class MedicineOrderDao extends DaoCrudOperations<Integer, MedicineOrder>
                              implements dao.MedicineOrderDao {

    @Override
    public List<MedicineOrder> findAll() {
        return PersistenceEntityManagerUtils.doTransaction(em -> {
            var query =
                    em.createQuery("""
                        from models.MedicineOrder
                        order by orderDate desc
                        """, MedicineOrder.class);
            return query.getResultList();
        });
    }

    @Override
    public List<MedicineOrder> findAllInPeriod(Instant begin, Instant end) {
        return PersistenceEntityManagerUtils.doTransaction(em -> {
            var query = em.createQuery("""
                from models.MedicineOrder
                where orderDate between :beginDate and :endDate
                """,
                MedicineOrder.class
            );
            return query.setParameter("beginDate", begin)
                        .setParameter("endDate", end)
                        .getResultList();
        });
    }

    @Override
    public int calcTotalCost() {
        return PersistenceEntityManagerUtils.doTransaction(em -> {
            var query = em.createQuery("""
                select sum(totalPrice)
                from models.MedicineOrder
                """,
                Long.class
            );
            var res = query.getSingleResult();
            return res == null ? 0 : res.intValue();
        });
    }

    @Override
    public int calcTotalCost(Instant begin, Instant end) {
        return PersistenceEntityManagerUtils.doTransaction(em -> {
            var query = em.createQuery("""
                select sum(totalPrice)
                from models.MedicineOrder
                where orderDate between :beginDate and :endDate
                """,
                Long.class
            );
            var res = query.setParameter("beginDate", begin)
                               .setParameter("endDate", end)
                               .getSingleResult();
            return res == null ? 0 : res.intValue();
        });
    }

    @Override
    public void orderMedicine(int medId, int count) {
        PersistenceEntityManagerUtils.doTransaction(em -> {
            var medicine = em.find(Medicine.class, medId);
            medicine.setCount(medicine.getCount() - count);
            em.merge(medicine);
            em.persist(new MedicineOrder(medicine, count));
        });
    }
}
